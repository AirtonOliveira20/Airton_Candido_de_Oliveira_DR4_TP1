package br.edu.infnet.ecommerce.integracao.pagamento;

import java.math.BigDecimal;


public record SolicitacaoPagamento(Long pedidoId,
                                   Long usuarioId,
                                   BigDecimal valor,
                                   String formaPagamento,
                                   String numeroCartao) {

    @Override
    public String toString() {   
        return "SolicitacaoPagamento[pedidoId=" + pedidoId + ", usuarioId=" + usuarioId
                + ", valor=" + valor + ", formaPagamento=" + formaPagamento + "]";
    }
}
