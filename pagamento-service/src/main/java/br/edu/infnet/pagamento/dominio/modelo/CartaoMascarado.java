package br.edu.infnet.pagamento.dominio.modelo;

/**
 * Value Object: o que o dominio GUARDA do cartao (so os 4 ultimos digitos).
 * O numero completo nunca entra no Aggregate nem e persistido.
 */
public record CartaoMascarado(String ultimosDigitos) {

    public CartaoMascarado {
        if (ultimosDigitos == null || !ultimosDigitos.matches("\\d{4}")) {
            ultimosDigitos = "????";
        }
    }

    public static CartaoMascarado de(DadosCartao cartao) {
        return new CartaoMascarado(cartao.ultimosQuatroDigitos());
    }

    /** Mesmo formato exibido pelo monolito: "**** **** **** 1111". */
    public String formatado() {
        return "**** **** **** " + ultimosDigitos;
    }
}
