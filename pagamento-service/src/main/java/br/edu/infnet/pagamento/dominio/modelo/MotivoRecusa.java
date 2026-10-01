package br.edu.infnet.pagamento.dominio.modelo;

/** Motivos de recusa - mesmos codigos usados pelo monolito, para manter compatibilidade. */
public enum MotivoRecusa {
    VALOR_INVALIDO,
    FORMA_PAGAMENTO_NAO_SUPORTADA,
    CARTAO_INVALIDO,
    LIMITE_EXCEDIDO,
    CARTAO_BLOQUEADO,
    RECUSADO_PELO_EMISSOR
}
