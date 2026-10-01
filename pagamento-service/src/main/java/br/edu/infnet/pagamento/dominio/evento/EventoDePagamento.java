package br.edu.infnet.pagamento.dominio.evento;

import br.edu.infnet.pagamento.dominio.modelo.PagamentoId;
import br.edu.infnet.pagamento.dominio.modelo.PedidoId;

import java.time.Instant;

/** Evento de dominio: algo relevante que ACONTECEU com um pagamento (nome no passado). */
public sealed interface EventoDePagamento permits PagamentoAprovado, PagamentoRecusado, PagamentoEstornado {

    PagamentoId pagamentoId();

    PedidoId pedidoId();

    Instant ocorridoEm();
}
