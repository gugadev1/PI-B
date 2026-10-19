package com.technova.validation;

import com.technova.domain.Cliente;
import com.technova.exception.ValidacaoException;

/**
 * Responsavel exclusivamente por validar os dados de um Cliente (SRP).
 * Retira a responsabilidade de validacao de regras de negocio de dentro do ClienteDAO.
 */
public class ClienteValidator implements Validador<Cliente> {

    @Override
    public void validar(Cliente cliente) {
        if (cliente == null) {
            throw new ValidacaoException("Cliente nao pode ser nulo.");
        }
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new ValidacaoException("Nome do cliente e obrigatorio.");
        }
        if (cliente.getEmail() == null || cliente.getEmail().isBlank() || !cliente.getEmail().contains("@")) {
            throw new ValidacaoException("Email do cliente e obrigatorio e deve ser valido.");
        }
        if (cliente.getTelefone() == null || cliente.getTelefone().isBlank()) {
            throw new ValidacaoException("Telefone do cliente e obrigatorio.");
        }
    }
}
