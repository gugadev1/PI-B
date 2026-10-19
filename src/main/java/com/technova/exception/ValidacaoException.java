package com.technova.exception;

/**
 * Excecao lancada quando dados de entrada de uma entidade sao invalidos.
 */
public class ValidacaoException extends RegraNegocioException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
