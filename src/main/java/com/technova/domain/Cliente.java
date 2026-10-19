package com.technova.domain;

public class Cliente extends Usuario {
    private String telefone;

    public Cliente() {
        super();
        setPerfil(PerfilUsuario.CLIENTE);
    }

    public Cliente(int id, String nome, String email, String senhaHash, String telefone) {
        super(id, nome, email, senhaHash, PerfilUsuario.CLIENTE);
        this.telefone = telefone;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
