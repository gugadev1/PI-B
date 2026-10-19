package com.technova.domain;

/**
 * Perfis de acesso dos usuarios do sistema TechNova.
 * Encapsula as permissoes de cada perfil conforme requisitos RF001, RF002 e RF006.
 */
public enum PerfilUsuario {
    ADMINISTRADOR(true, true),
    FUNCIONARIO(false, true),
    CLIENTE(false, false);

    private final boolean podeRemoverProdutos;
    private final boolean podeAlterarStatusPedido;

    PerfilUsuario(boolean podeRemoverProdutos, boolean podeAlterarStatusPedido) {
        this.podeRemoverProdutos = podeRemoverProdutos;
        this.podeAlterarStatusPedido = podeAlterarStatusPedido;
    }

    public boolean podeRemoverProdutos() {
        return podeRemoverProdutos;
    }

    public boolean podeAlterarStatusPedido() {
        return podeAlterarStatusPedido;
    }
}
