package br.edu.infnet.ecommerce.service;

import br.edu.infnet.ecommerce.entity.Pagamento;
import br.edu.infnet.ecommerce.entity.Pedido;
import br.edu.infnet.ecommerce.entity.Usuario;
import br.edu.infnet.ecommerce.payment.ProcessadorPagamento;
import br.edu.infnet.ecommerce.payment.ResultadoProcessamento;
import br.edu.infnet.ecommerce.repository.PagamentoRepository;
import br.edu.infnet.ecommerce.repository.PedidoRepository;
import br.edu.infnet.ecommerce.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PagamentoService {

    /*
     * Serviço deliberadamente acoplado:
     * - usa uma implementação concreta;
     * - acessa repositórios de Pedido e Usuário;
     * - recebe entidades JPA;
     * - concentra persistência e integração externa simulada.
     */
    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProcessadorPagamento processadorPagamento;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository,
            ProcessadorPagamento processadorPagamento
    ) {
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.processadorPagamento = processadorPagamento;
    }

    public Pagamento processar(
            Pedido pedido,
            Usuario usuario,
            BigDecimal valor,
            String formaPagamento,
            String numeroCartao
    ) {
        // Consultas redundantes intencionais.
        Pedido pedidoPersistido = pedidoRepository.findById(pedido.getId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado"));

        Usuario usuarioPersistido = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        ResultadoProcessamento resultado = processadorPagamento.processar(
                valor,
                formaPagamento,
                numeroCartao
        );

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedidoPersistido);
        pagamento.setUsuario(usuarioPersistido);
        pagamento.setValor(valor);
        pagamento.setFormaPagamento(formaPagamento);
        pagamento.setNumeroCartaoMascarado(mascarar(numeroCartao));
        pagamento.setStatus(resultado.status());
        pagamento.setMotivo(resultado.motivo());
        pagamento.setCodigoAutorizacao(resultado.codigoAutorizacao());
        pagamento.setProcessadoEm(LocalDateTime.now());

        return pagamentoRepository.save(pagamento);
    }

    private String mascarar(String numeroCartao) {
        if (numeroCartao == null || numeroCartao.length() < 4) {
            return "****";
        }

        return "**** **** **** " + numeroCartao.substring(numeroCartao.length() - 4);
    }
}
