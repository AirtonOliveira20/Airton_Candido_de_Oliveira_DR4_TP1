package br.edu.infnet.pagamento.dominio.modelo;

/**
 * Value Object: quem paga. No contexto de Pagamento o "Usuario" do monolito e um Cliente (pagador),
 * referenciado so pelo ID - sem @ManyToOne com a entidade Usuario.
 */
public record ClienteId(Long valor) {

    public ClienteId {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("ClienteId invalido: " + valor);
        }
    }
}
