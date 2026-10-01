package br.edu.infnet.ecommerce.integracao.pagamento;

import br.edu.infnet.ecommerce.integracao.pagamento.ResultadoPagamento.OrigemPagamento;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Duration;


@Component
public class PagamentoMicrosservicoAdapter implements GatewayDePagamento {

    private static final Logger log = LoggerFactory.getLogger(PagamentoMicrosservicoAdapter.class);

    private final RestClient http;

    public PagamentoMicrosservicoAdapter(@Value("${migracao.pagamento.url}") String baseUrl,
                                         @Value("${migracao.pagamento.timeout-ms:3000}") long timeoutMs) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(fabrica).build();
    }

    @Override
    public ResultadoPagamento processar(SolicitacaoPagamento s) {
        RespostaApi resposta = enviar(s, false);
        if (resposta.aprovado()) {
            registrarCompensacao(resposta.pagamentoId());
        }
        return traduzir(resposta);
    }

    /** Modo sombra: o microsservico avalia, mas nao grava nem cobra. */
    public ResultadoPagamento simular(SolicitacaoPagamento s) {
        return traduzir(enviar(s, true));
    }

    private RespostaApi enviar(SolicitacaoPagamento s, boolean simulacao) {
        PedidoApi corpo = new PedidoApi(
                s.pedidoId(), s.usuarioId(), s.valor(), s.formaPagamento(), s.numeroCartao());
        try {
            RespostaApi resposta = http.post()
                    .uri(uri -> uri.path("/api/pagamentos").queryParam("simulacao", simulacao).build())
                    .body(corpo)
                    .retrieve()
                    .body(RespostaApi.class);
            if (resposta == null) {
                throw new PagamentoIndisponivelException("Resposta vazia do pagamento-service", null);
            }
            return resposta;
        } catch (RestClientException e) {
            throw new PagamentoIndisponivelException("pagamento-service indisponivel: " + e.getMessage(), e);
        }
    }

    /**
     * Saga simplificada: o pagamento foi APROVADO no microsservico, mas a transacao do pedido
     * ainda pode falhar no monolito (ex.: erro ao gravar). Se ela sofrer rollback, pedimos o estorno.
     * Sem isso o cliente ficaria cobrado por um pedido que nao existe.
     */
    private void registrarCompensacao(String pagamentoId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        http.post().uri("/api/pagamentos/{id}/estorno", pagamentoId)
                                .retrieve().toBodilessEntity();
                        log.warn("Pedido desfeito: pagamento {} estornado", pagamentoId);
                    } catch (RestClientException e) {
                        // em producao: gravar numa tabela/fila de pendencias para reprocessar
                        log.error("FALHA ao estornar pagamento {} - requer acao manual", pagamentoId, e);
                    }
                }
            }
        });
    }

    /** Traducao do modelo do novo contexto para o modelo do monolito. */
    private ResultadoPagamento traduzir(RespostaApi r) {
        return new ResultadoPagamento(
                r.aprovado(),
                r.aprovado() ? "APROVADO" : "RECUSADO",
                r.motivo(),
                r.codigoAutorizacao(),
                r.pagamentoId(),
                OrigemPagamento.MICROSSERVICO);
    }

    // ----- contrato HTTP do pagamento-service: visivel apenas dentro do ACL -----

    record PedidoApi(Long pedidoId, Long clienteId, BigDecimal valor,
                             String formaPagamento, String numeroCartao) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaApi(String pagamentoId, boolean aprovado, String status,
                               String motivo, String codigoAutorizacao) {
    }
}
