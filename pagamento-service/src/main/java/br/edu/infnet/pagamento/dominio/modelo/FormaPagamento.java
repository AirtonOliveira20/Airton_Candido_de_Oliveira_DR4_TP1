package br.edu.infnet.pagamento.dominio.modelo;

import java.util.Optional;

/** Formas de pagamento conhecidas pelo dominio. No monolito era uma String livre. */
public enum FormaPagamento {
    CARTAO;

    /** Converte o texto vindo de fora; vazio se a forma nao for suportada. */
    public static Optional<FormaPagamento> de(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(FormaPagamento.valueOf(texto.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
