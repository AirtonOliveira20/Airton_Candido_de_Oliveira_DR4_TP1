package br.edu.infnet.pagamento.dominio.modelo;

/**
 * Value Object: referencia ao Pedido (outro Bounded Context) APENAS PELO ID.
 * No monolito, Pagamento tinha @OneToOne com a entidade Pedido; aqui nao existe mais esse acoplamento.
 */
public record PedidoId(Long valor) {

    public PedidoId {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("PedidoId invalido: " + valor);
        }
    }
}
