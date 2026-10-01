package br.edu.infnet.pagamento.infraestrutura;

import br.edu.infnet.pagamento.dominio.modelo.Dinheiro;
import br.edu.infnet.pagamento.dominio.modelo.PoliticaDePagamento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Clock;

/** Monta os objetos de dominio que dependem de configuracao (o dominio continua livre de Spring). */
@Configuration
public class ConfiguracaoDominio {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    PoliticaDePagamento politicaDePagamento(@Value("${pagamento.limite-por-transacao}") BigDecimal limite) {
        return new PoliticaDePagamento(new Dinheiro(limite));
    }
}
