package br.edu.infnet.pagamento.dominio.modelo;

/**
 * Value Object TRANSITORIO: numero completo do cartao, usado apenas para pedir autorizacao ao gateway.
 * Nao e atributo do Aggregate e nao e persistido (toString nao expoe o numero).
 */
public record DadosCartao(String numero) {

    public boolean ehValido() {
        return numero != null && numero.length() >= 4;
    }

    public String ultimosQuatroDigitos() {
        return ehValido() ? numero.substring(numero.length() - 4) : null;
    }

    @Override
    public String toString() {
        return "DadosCartao[" + CartaoMascarado.de(this).formatado() + "]";
    }
}
