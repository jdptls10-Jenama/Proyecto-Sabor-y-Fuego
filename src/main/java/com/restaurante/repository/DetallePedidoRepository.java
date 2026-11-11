
package com.restaurante.repository;

import com.restaurante.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    // Consultar los productos que pertenecen a un pedido
    List<DetallePedido> findByPedido_Id(Long pedidoId);
}
