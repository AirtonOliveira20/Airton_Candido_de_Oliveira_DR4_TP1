package br.edu.infnet.pagamento.infraestrutura.evento;

import br.edu.infnet.pagamento.dominio.evento.EventoDePagamento;
import br.edu.infnet.pagamento.dominio.porta.PublicadorDeEventos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Publica os eventos dentro do processo (ApplicationEventPublisher) e registra em log.
 * Proximo passo da migracao: Transactional Outbox + broker (Kafka/RabbitMQ), para que o
 * contexto de Pedido reaja a PagamentoAprovado/PagamentoRecusado de forma assincrona.
 */
@Component
public class PublicadorDeEventosSpring implements PublicadorDeEventos {

    private static final Logger log = LoggerFactory.getLogger(PublicadorDeEventosSpring.class);

    private final ApplicationEventPublisher publisher;

    public PublicadorDeEventosSpring(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(List<EventoDePagamento> eventos) {
        for (EventoDePagamento evento : eventos) {
            log.info("Evento de dominio: {}", evento);
            publisher.publishEvent(evento);
        }
    }
}
