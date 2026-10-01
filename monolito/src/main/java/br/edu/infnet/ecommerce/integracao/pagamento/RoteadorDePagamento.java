package br.edu.infnet.ecommerce.integracao.pagamento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Primary
@Component
public class RoteadorDePagamento implements GatewayDePagamento {

    private static final Logger log = LoggerFactory.getLogger(RoteadorDePagamento.class);

    private final ModoPagamento modo;
    private final PagamentoLegadoAdapter legado;
    private final PagamentoMicrosservicoAdapter microsservico;

    public RoteadorDePagamento(@Value("${migracao.pagamento.modo:LEGADO}") ModoPagamento modo,
                               PagamentoLegadoAdapter legado,
                               PagamentoMicrosservicoAdapter microsservico) {
        this.modo = modo;
        this.legado = legado;
        this.microsservico = microsservico;
        log.info("Pagamentos atendidos em modo {}", modo);
    }

    @Override
    public ResultadoPagamento processar(SolicitacaoPagamento solicitacao) {
        return switch (modo) {
            case LEGADO -> legado.processar(solicitacao);
            case MICROSSERVICO -> microsservico.processar(solicitacao);
            case SOMBRA -> {
                ResultadoPagamento oficial = legado.processar(solicitacao);
                compararEmSombra(solicitacao, oficial);
                yield oficial;
            }
        };
    }

    /** Executa o microsservico em paralelo; QUALQUER falha aqui e ignorada (nao afeta o cliente). */
    private void compararEmSombra(SolicitacaoPagamento solicitacao, ResultadoPagamento oficial) {
        try {
            ResultadoPagamento novo = microsservico.simular(solicitacao);
            boolean igual = novo.aprovado() == oficial.aprovado()
                    && Objects.equals(novo.motivo(), oficial.motivo());
            if (igual) {
                log.info("[SOMBRA] pedido {}: resultados iguais ({})", solicitacao.pedidoId(), oficial.status());
            } else {
                log.warn("[SOMBRA] DIVERGENCIA no pedido {}: legado={}/{} microsservico={}/{}",
                        solicitacao.pedidoId(), oficial.status(), oficial.motivo(), novo.status(), novo.motivo());
            }
        } catch (RuntimeException e) {
            log.warn("[SOMBRA] microsservico falhou para o pedido {}: {}", solicitacao.pedidoId(), e.getMessage());
        }
    }
}
