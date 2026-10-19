package com.technova.repository.memory;

import com.technova.domain.Cliente;
import com.technova.repository.ClienteRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacao em memoria de ClienteRepository.
 */
public class ClienteRepositoryMemoria implements ClienteRepository {
    private final List<Cliente> clientes = new ArrayList<>();
    private int proximoId = 1;

    @Override
    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() <= 0) {
            cliente.setId(proximoId++);
        } else if (cliente.getId() >= proximoId) {
            proximoId = cliente.getId() + 1;
        }
        clientes.add(cliente);
        return cliente;
    }

    @Override
    public Cliente atualizar(Cliente cliente) {
        Optional<Cliente> existenteOpt = buscarPorId(cliente.getId());
        if (existenteOpt.isPresent()) {
            Cliente existente = existenteOpt.get();
            existente.setNome(cliente.getNome());
            existente.setEmail(cliente.getEmail());
            existente.setSenhaHash(cliente.getSenhaHash());
            existente.setTelefone(cliente.getTelefone());
            return existente;
        }
        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        return clientes.stream().filter(c -> c.getId() == id).findFirst();
    }

    @Override
    public List<Cliente> listarTodos() {
        return new ArrayList<>(clientes);
    }
}
