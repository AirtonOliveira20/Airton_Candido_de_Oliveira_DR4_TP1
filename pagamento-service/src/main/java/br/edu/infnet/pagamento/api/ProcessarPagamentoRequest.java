package br.edu.infnet.pagamento.api;

import br.edu.infnet.pagamento.aplicacao.ProcessarPagamentoComando;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Corpo do POST /api/pagamentos (contrato HTTP do microsservico). */
public record ProcessarPagamentoRequest(
        @NotNull @Positive Long pedidoId,
        @NotNull @Positive Long clienteId,
        @NotNull BigDecimal valor,
        @NotBlank String formaPagamento,
        @NotBlank String numeroCartao
) {
    ProcessarPagamentoComando paraComando() {
        return new ProcessarPagamentoComando(pedidoId, clienteId, valor, formaPagamento, numeroCartao);
    }

    @Override
    public String toString() {
        return paraComando().toString();
    }
}
