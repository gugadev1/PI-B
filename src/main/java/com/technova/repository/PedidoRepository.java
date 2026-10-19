package com.technova.repository;

import com.technova.domain.Pedido;
import com.technova.domain.StatusPedido;
import java.util.List;
import java.util.Optional;

/**
 * Interface de repositorio para persistencia de Pedidos (DIP e ISP).
 */
public interface PedidoRepository {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(int id);
    List<Pedido> listarTodos();
    List<Pedido> listarPorCliente(int idCliente);
    boolean atualizarStatus(int idPedido, StatusPedido novoStatus);
}
