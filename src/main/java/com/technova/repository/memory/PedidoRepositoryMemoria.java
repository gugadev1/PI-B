package com.technova.repository.memory;

import com.technova.domain.Pedido;
import com.technova.domain.StatusPedido;
import com.technova.repository.PedidoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementacao em memoria de PedidoRepository.
 * Substitui a lista de pedidos e controle manual de IDs que ficavam em TechNovaDesktopUI.
 */
public class PedidoRepositoryMemoria implements PedidoRepository {
    private final List<Pedido> pedidos = new ArrayList<>();
    private int proximoId = 1;

    @Override
    public Pedido salvar(Pedido pedido) {
        if (pedido.getId() <= 0) {
            pedido.setId(proximoId++);
        } else if (pedido.getId() >= proximoId) {
            proximoId = pedido.getId() + 1;
        }
        pedidos.add(pedido);
        return pedido;
    }

    @Override
    public Optional<Pedido> buscarPorId(int id) {
        return pedidos.stream().filter(p -> p.getId() == id).findFirst();
    }

    @Override
    public List<Pedido> listarTodos() {
        return new ArrayList<>(pedidos);
    }

    @Override
    public List<Pedido> listarPorCliente(int idCliente) {
        return pedidos.stream()
                .filter(p -> p.getCliente() != null && p.getCliente().getId() == idCliente)
                .collect(Collectors.toList());
    }

    @Override
    public boolean atualizarStatus(int idPedido, StatusPedido novoStatus) {
        Optional<Pedido> pedidoOpt = buscarPorId(idPedido);
        if (pedidoOpt.isPresent() && novoStatus != null) {
            pedidoOpt.get().setStatus(novoStatus);
            return true;
        }
        return false;
    }
}
