package br.edu.infnet.pagamento.infraestrutura.persistencia;

import br.edu.infnet.pagamento.dominio.modelo.CartaoMascarado;
import br.edu.infnet.pagamento.dominio.modelo.ClienteId;
import br.edu.infnet.pagamento.dominio.modelo.Dinheiro;
import br.edu.infnet.pagamento.dominio.modelo.FormaPagamento;
import br.edu.infnet.pagamento.dominio.modelo.MotivoRecusa;
import br.edu.infnet.pagamento.dominio.modelo.Pagamento;
import br.edu.infnet.pagamento.dominio.modelo.PagamentoId;
import br.edu.infnet.pagamento.dominio.modelo.PedidoId;
import br.edu.infnet.pagamento.dominio.modelo.StatusPagamento;
import br.edu.infnet.pagamento.dominio.porta.Pagamentos;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** ADAPTER de saida: implementa a porta Pagamentos com Spring Data JPA, traduzindo dominio <-> tabela. */
@Repository
public class PagamentosJpaAdapter implements Pagamentos {

    private final PagamentoSpringDataRepository jpa;

    public PagamentosJpaAdapter(PagamentoSpringDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Pagamento salvar(Pagamento p) {
        jpa.save(paraEntidade(p));
        return p;
    }

    @Override
    public Optional<Pagamento> porId(PagamentoId id) {
        return jpa.findById(id.valor()).map(this::paraDominio);
    }

    @Override
    public Optional<Pagamento> porPedido(PedidoId pedidoId) {
        return jpa.findByPedidoId(pedidoId.valor()).map(this::paraDominio);
    }

    private PagamentoJpaEntity paraEntidade(Pagamento p) {
        return new PagamentoJpaEntity(
                p.id().valor(),
                p.pedidoId().valor(),
                p.clienteId().valor(),
                p.valor().valor(),
                p.forma().map(Enum::name).orElse(null),
                p.cartao().ultimosDigitos(),
                p.status().name(),
                p.motivoRecusa().map(Enum::name).orElse(null),
                p.codigoAutorizacao().orElse(null),
                p.criadoEm(),
                p.processadoEm().orElse(null));
    }

    private Pagamento paraDominio(PagamentoJpaEntity e) {
        return Pagamento.reconstituir(
                new PagamentoId(e.getId()),
                new PedidoId(e.getPedidoId()),
                new ClienteId(e.getClienteId()),
                new Dinheiro(e.getValor()),
                e.getFormaPagamento() == null ? null : FormaPagamento.valueOf(e.getFormaPagamento()),
                new CartaoMascarado(e.getCartaoUltimosDigitos()),
                StatusPagamento.valueOf(e.getStatus()),
                e.getMotivoRecusa() == null ? null : MotivoRecusa.valueOf(e.getMotivoRecusa()),
                e.getCodigoAutorizacao(),
                e.getCriadoEm(),
                e.getProcessadoEm());
    }
}
