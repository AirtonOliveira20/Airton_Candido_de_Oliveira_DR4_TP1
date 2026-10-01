package br.edu.infnet.pagamento.dominio.modelo;

/**
 * Ciclo de vida do pagamento:
 *   PENDENTE --aprovar--> APROVADO --estornar--> ESTORNADO
 *   PENDENTE --recusar--> RECUSADO
 */
public enum StatusPagamento {
    PENDENTE,
    APROVADO,
    RECUSADO,
    ESTORNADO
}
