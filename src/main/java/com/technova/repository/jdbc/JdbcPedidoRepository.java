package com.technova.repository.jdbc;

import com.technova.domain.Pedido;
import com.technova.domain.StatusPedido;
import com.technova.repository.PedidoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPedidoRepository implements PedidoRepository {
    private final JdbcTechNovaRepository database;

    public JdbcPedidoRepository(JdbcTechNovaRepository database) {
        this.database = database;
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        return database.salvarPedido(pedido);
    }

    @Override
    public Optional<Pedido> buscarPorId(int id) {
        return database.buscarPedidoPorId(id);
    }

    @Override
    public List<Pedido> listarTodos() {
        return database.listarTodosPedidos();
    }

    @Override
    public List<Pedido> listarPorCliente(int idCliente) {
        return database.listarPedidosPorCliente(idCliente);
    }

    @Override
    public boolean atualizarStatus(int idPedido, StatusPedido novoStatus) {
        return database.atualizarStatus(idPedido, novoStatus);
    }

    @Override
    public boolean atualizarStatus(int idPedido, StatusPedido statusAtual, StatusPedido novoStatus) {
        return database.atualizarStatusCondicional(idPedido, statusAtual, novoStatus);
    }
}
