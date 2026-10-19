package com.technova.exception;

/**
 * Excecao lancada em falhas na camada de persistencia (substitui o retorno silencioso de false nos DAOs).
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }
}
