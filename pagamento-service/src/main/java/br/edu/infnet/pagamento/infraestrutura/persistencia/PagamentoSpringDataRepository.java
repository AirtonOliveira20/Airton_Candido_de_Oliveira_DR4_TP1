package br.edu.infnet.pagamento.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PagamentoSpringDataRepository extends JpaRepository<PagamentoJpaEntity, UUID> {

    Optional<PagamentoJpaEntity> findByPedidoId(Long pedidoId);
}
