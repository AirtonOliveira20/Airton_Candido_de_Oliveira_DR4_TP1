package br.edu.infnet.pagamento.dominio.porta;

import br.edu.infnet.pagamento.dominio.modelo.Pagamento;
import br.edu.infnet.pagamento.dominio.modelo.PagamentoId;
import br.edu.infnet.pagamento.dominio.modelo.PedidoId;

import java.util.Optional;

/**
 * PORTA DE SAIDA (Repository do DDD): colecao de Aggregates Pagamento.
 * O dominio define o contrato; a infraestrutura (JPA) implementa.
 */
public interface Pagamentos {

    Pagamento salvar(Pagamento pagamento);

    Optional<Pagamento> porId(PagamentoId id);

    Optional<Pagamento> porPedido(PedidoId pedidoId);
}
