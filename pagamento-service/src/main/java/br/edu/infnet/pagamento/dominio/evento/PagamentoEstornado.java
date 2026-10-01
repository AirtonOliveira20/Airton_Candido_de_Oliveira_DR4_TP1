package br.edu.infnet.pagamento.dominio.evento;

import br.edu.infnet.pagamento.dominio.modelo.Dinheiro;
import br.edu.infnet.pagamento.dominio.modelo.PagamentoId;
import br.edu.infnet.pagamento.dominio.modelo.PedidoId;

import java.time.Instant;

public record PagamentoEstornado(PagamentoId pagamentoId, PedidoId pedidoId, Dinheiro valor,
                                 Instant ocorridoEm) implements EventoDePagamento {
}
