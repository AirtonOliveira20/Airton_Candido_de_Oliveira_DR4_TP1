package br.edu.infnet.ecommerce.integracao.pagamento;


public record ResultadoPagamento(boolean aprovado,
                                 String status,
                                 String motivo,
                                 String codigoAutorizacao,
                                 String referenciaExterna,
                                 OrigemPagamento origem) {

    public enum OrigemPagamento { LEGADO, MICROSSERVICO }
}
