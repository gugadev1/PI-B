package com.technova.validation;

/**
 * Contrato generico para validadores de entidades do dominio (OCP / Strategy).
 * @param <T> Tipo da entidade a ser validada.
 */
public interface Validador<T> {
    void validar(T entidade);
}
