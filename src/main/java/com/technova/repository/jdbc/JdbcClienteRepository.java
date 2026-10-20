package com.technova.repository.jdbc;

import com.technova.domain.Cliente;
import com.technova.repository.ClienteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcClienteRepository implements ClienteRepository {
    private final JdbcTechNovaRepository database;

    public JdbcClienteRepository(JdbcTechNovaRepository database) {
        this.database = database;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return database.salvarCliente(cliente);
    }

    @Override
    public Cliente atualizar(Cliente cliente) {
        return database.atualizarCliente(cliente);
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        return database.buscarClientePorId(id);
    }

    @Override
    public List<Cliente> listarTodos() {
        return database.listarClientes();
    }
}
