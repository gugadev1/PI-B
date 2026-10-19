package com.technova.repository;

import com.technova.domain.Cliente;
import java.util.List;
import java.util.Optional;

/**
 * Interface de repositorio para persistencia de Clientes (DIP e ISP).
 */
public interface ClienteRepository {
    Cliente salvar(Cliente cliente);
    Cliente atualizar(Cliente cliente);
    Optional<Cliente> buscarPorId(int id);
    List<Cliente> listarTodos();
}
