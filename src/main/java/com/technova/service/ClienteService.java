package com.technova.service;

import com.technova.domain.Cliente;
import com.technova.exception.RegraNegocioException;
import com.technova.repository.ClienteRepository;
import com.technova.validation.ClienteValidator;
import com.technova.validation.Validador;
import java.util.List;

/**
 * Servico de regras de negocio para gerenciamento de Clientes (RF003).
 * Aplica SRP e DIP ao separar validacoes da camada de persistencia.
 */
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final Validador<Cliente> validador;

    public ClienteService(ClienteRepository clienteRepository) {
        this(clienteRepository, new ClienteValidator());
    }

    public ClienteService(ClienteRepository clienteRepository, Validador<Cliente> validador) {
        this.clienteRepository = clienteRepository;
        this.validador = validador;
    }

    public Cliente cadastrarCliente(Cliente cliente) {
        validador.validar(cliente);
        return clienteRepository.salvar(cliente);
    }

    public Cliente atualizarCliente(Cliente cliente) {
        validador.validar(cliente);
        if (cliente.getId() <= 0 || clienteRepository.buscarPorId(cliente.getId()).isEmpty()) {
            throw new RegraNegocioException("Cliente nao encontrado para atualizacao.");
        }
        return clienteRepository.atualizar(cliente);
    }

    public Cliente buscarPorId(int idCliente) {
        return clienteRepository.buscarPorId(idCliente)
                .orElseThrow(() -> new RegraNegocioException("Cliente nao encontrado: ID " + idCliente));
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.listarTodos();
    }
}
