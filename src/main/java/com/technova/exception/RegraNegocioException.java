package com.technova.exception;

/**
 * Excecao lancada quando uma regra de negocio do dominio TechNova e violada.
 * Permite desacoplar o tratamento de erros da camada de interface grafica (Swing/Web).
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    public RegraNegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
