package br.edu.infnet.pagamento.aplicacao;

import java.math.BigDecimal;

/** Entrada do caso de uso (dados primitivos vindos de fora; o servico converte para Value Objects). */
public record ProcessarPagamentoComando(Long pedidoId, Long clienteId, BigDecimal valor,
                                        String formaPagamento, String numeroCartao) {

    @Override
    public String toString() {   // nunca loga o numero do cartao
        return "ProcessarPagamentoComando[pedidoId=" + pedidoId + ", clienteId=" + clienteId
                + ", valor=" + valor + ", formaPagamento=" + formaPagamento + "]";
    }
}
