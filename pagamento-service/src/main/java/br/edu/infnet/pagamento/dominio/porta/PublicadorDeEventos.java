package br.edu.infnet.pagamento.dominio.porta;

import br.edu.infnet.pagamento.dominio.evento.EventoDePagamento;

import java.util.List;

/** PORTA DE SAIDA para publicar Eventos de Dominio (hoje: Spring events + log; amanha: Kafka/RabbitMQ). */
public interface PublicadorDeEventos {

    void publicar(List<EventoDePagamento> eventos);
}
