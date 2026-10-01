package br.edu.infnet.ecommerce.payment;

public record ResultadoProcessamento(
        boolean aprovado,
        String status,
        String motivo,
        String codigoAutorizacao
) {
    public static ResultadoProcessamento aprovado(String codigoAutorizacao) {
        return new ResultadoProcessamento(true, "APROVADO", null, codigoAutorizacao);
    }

    public static ResultadoProcessamento recusado(String motivo) {
        return new ResultadoProcessamento(false, "RECUSADO", motivo, null);
    }
}
