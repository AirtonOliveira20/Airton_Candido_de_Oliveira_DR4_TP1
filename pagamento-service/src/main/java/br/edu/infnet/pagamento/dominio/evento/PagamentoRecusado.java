package br.edu.infnet.pagamento.dominio.evento;

import br.edu.infnet.pagamento.dominio.modelo.MotivoRecusa;
import br.edu.infnet.pagamento.dominio.modelo.PagamentoId;
import br.edu.infnet.pagamento.dominio.modelo.PedidoId;

import java.time.Instant;

public record PagamentoRecusado(PagamentoId pagamentoId, PedidoId pedidoId, MotivoRecusa motivo,
                                Instant ocorridoEm) implements EventoDePagamento {
}
