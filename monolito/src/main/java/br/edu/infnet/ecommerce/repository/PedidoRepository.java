package br.edu.infnet.ecommerce.repository;

import br.edu.infnet.ecommerce.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
