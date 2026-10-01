package br.edu.infnet.pagamento.dominio.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object monetario: imutavel, sempre com 2 casas decimais e comparado por valor.
 * Substitui o BigDecimal "solto" do monolito.
 */
public record Dinheiro(BigDecimal valor) {

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        Objects.requireNonNull(valor, "Valor monetario nao pode ser nulo");
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public boolean ehPositivo() {
        return valor.signum() > 0;
    }

    public boolean ehMaiorQue(Dinheiro outro) {
        return valor.compareTo(outro.valor) > 0;
    }

    @Override
    public String toString() {
        return "R$ " + valor.toPlainString();
    }
}
