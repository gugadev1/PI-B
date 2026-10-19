package com.technova.domain;

/**
 * Status do ciclo de vida de um pedido no sistema TechNova.
 * Encapsula a regra de transicao controlada (EM_PROCESSAMENTO -> APROVADO -> ENVIADO -> ENTREGUE / CANCELADO).
 */
public enum StatusPedido {
    EM_PROCESSAMENTO,
    APROVADO,
    ENVIADO,
    ENTREGUE,
    CANCELADO;

    public boolean podeTransicionarPara(StatusPedido novoStatus) {
        if (novoStatus == null || this == novoStatus) {
            return false;
        }
        return switch (this) {
            case EM_PROCESSAMENTO -> novoStatus == APROVADO || novoStatus == CANCELADO;
            case APROVADO -> novoStatus == ENVIADO || novoStatus == CANCELADO;
            case ENVIADO -> novoStatus == ENTREGUE;
            case ENTREGUE, CANCELADO -> false;
        };
    }
}
