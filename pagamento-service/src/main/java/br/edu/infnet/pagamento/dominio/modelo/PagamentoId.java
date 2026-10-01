package br.edu.infnet.pagamento.dominio.modelo;

import java.util.Objects;
import java.util.UUID;

/** Value Object: identidade do Aggregate Pagamento (gerada pelo proprio dominio, nao pelo banco). */
public record PagamentoId(UUID valor) {

    public PagamentoId {
        Objects.requireNonNull(valor, "PagamentoId nao pode ser nulo");
    }

    public static PagamentoId novo() {
        return new PagamentoId(UUID.randomUUID());
    }

    public static PagamentoId de(String valor) {
        return new PagamentoId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
