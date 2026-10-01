package br.edu.infnet.pagamento.aplicacao;

import br.edu.infnet.pagamento.dominio.modelo.ClienteId;
import br.edu.infnet.pagamento.dominio.modelo.DadosCartao;
import br.edu.infnet.pagamento.dominio.modelo.Dinheiro;
import br.edu.infnet.pagamento.dominio.modelo.FormaPagamento;
import br.edu.infnet.pagamento.dominio.modelo.Pagamento;
import br.edu.infnet.pagamento.dominio.modelo.PagamentoId;
import br.edu.infnet.pagamento.dominio.modelo.PedidoId;
import br.edu.infnet.pagamento.dominio.modelo.PoliticaDePagamento;
import br.edu.infnet.pagamento.dominio.porta.GatewayDeAutorizacao;
import br.edu.infnet.pagamento.dominio.porta.GatewayDeAutorizacao.RespostaAutorizacao;
import br.edu.infnet.pagamento.dominio.porta.Pagamentos;
import br.edu.infnet.pagamento.dominio.porta.PublicadorDeEventos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;

/*Sem regra de negocio propria. As regras ficam no Aggregate (Pagamento) e na PoliticaDePagamento.*/
@Service
public class ServicoDePagamento {

    private final Pagamentos pagamentos;
    private final GatewayDeAutorizacao gateway;
    private final PublicadorDeEventos publicador;
    private final PoliticaDePagamento politica;
    private final Clock relogio;

    public ServicoDePagamento(Pagamentos pagamentos, GatewayDeAutorizacao gateway,
                              PublicadorDeEventos publicador, PoliticaDePagamento politica, Clock relogio) {
        this.pagamentos = pagamentos;
        this.gateway = gateway;
        this.publicador = publicador;
        this.politica = politica;
        this.relogio = relogio;
    }

    /**
     * Processa o pagamento de um pedido. IDEMPOTENTE por pedido: se o monolito repetir a chamada
     * (timeout, retry), o mesmo resultado e devolvido e o cliente NAO e cobrado duas vezes.
     */
    @Transactional
    public ResultadoPagamento processar(ProcessarPagamentoComando cmd) {
        PedidoId pedidoId = new PedidoId(cmd.pedidoId());

        Optional<Pagamento> existente = pagamentos.porPedido(pedidoId);
        if (existente.isPresent()) {
            return ResultadoPagamento.de(existente.get(), true);
        }

        Pagamento pagamento = criarEAutorizar(cmd, pedidoId);

        pagamentos.salvar(pagamento);
        publicador.publicar(pagamento.eventosPendentes());
        pagamento.limparEventos();
        return ResultadoPagamento.de(pagamento, false);
    }

    /**
     * Executa as mesmas regras SEM persistir nem publicar eventos.
     * Usado no modo "sombra" da migracao, para comparar o resultado com o do monolito.
     */
    public ResultadoPagamento simular(ProcessarPagamentoComando cmd) {
        return ResultadoPagamento.de(criarEAutorizar(cmd, new PedidoId(cmd.pedidoId())), false);
    }

    /** Compensacao (Saga): estorna um pagamento aprovado cujo pedido nao foi concluido. */
    @Transactional
    public ResultadoPagamento estornar(String pagamentoId) {
        Pagamento pagamento = pagamentos.porId(PagamentoId.de(pagamentoId))
                .orElseThrow(() -> new PagamentoNaoEncontradoException("Pagamento nao encontrado: " + pagamentoId));
        pagamento.estornar(relogio);
        pagamentos.salvar(pagamento);
        publicador.publicar(pagamento.eventosPendentes());
        pagamento.limparEventos();
        return ResultadoPagamento.de(pagamento, false);
    }

    @Transactional(readOnly = true)
    public ResultadoPagamento consultarPorPedido(Long pedidoId) {
        return pagamentos.porPedido(new PedidoId(pedidoId))
                .map(p -> ResultadoPagamento.de(p, true))
                .orElseThrow(() -> new PagamentoNaoEncontradoException("Nenhum pagamento para o pedido " + pedidoId));
    }

    @Transactional(readOnly = true)
    public ResultadoPagamento consultarPorId(String pagamentoId) {
        return pagamentos.porId(PagamentoId.de(pagamentoId))
                .map(p -> ResultadoPagamento.de(p, true))
                .orElseThrow(() -> new PagamentoNaoEncontradoException("Pagamento nao encontrado: " + pagamentoId));
    }

    private Pagamento criarEAutorizar(ProcessarPagamentoComando cmd, PedidoId pedidoId) {
        DadosCartao cartao = new DadosCartao(cmd.numeroCartao());
        Optional<FormaPagamento> forma = FormaPagamento.de(cmd.formaPagamento());

        // 1) regras do dominio (pode nascer RECUSADO sem envolver o gateway)
        Pagamento pagamento = Pagamento.solicitar(
                pedidoId, new ClienteId(cmd.clienteId()), new Dinheiro(cmd.valor()),
                forma, cartao, politica, relogio);

        // 2) autorizacao externa, so se passou pelas regras
        if (pagamento.aguardandoAutorizacao()) {
            RespostaAutorizacao resposta = gateway.autorizar(pagamento.valor(), cartao);
            if (resposta.aprovada()) {
                pagamento.aprovar(resposta.codigoAutorizacao(), relogio);
            } else {
                pagamento.recusar(resposta.motivo(), relogio);
            }
        }
        return pagamento;
    }
}
