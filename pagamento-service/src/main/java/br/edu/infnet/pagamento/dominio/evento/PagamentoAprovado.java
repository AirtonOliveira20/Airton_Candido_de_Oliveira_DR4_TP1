package br.edu.infnet.pagamento.dominio.evento;

import br.edu.infnet.pagamento.dominio.modelo.Dinheiro;
import br.edu.infnet.pagamento.dominio.modelo.PagamentoId;
import br.edu.infnet.pagamento.dominio.modelo.PedidoId;

import java.time.Instant;

public record PagamentoAprovado(PagamentoId pagamentoId, PedidoId pedidoId, Dinheiro valor,
                                String codigoAutorizacao, Instant ocorridoEm) implements EventoDePagamento {
}
