package com.technova.domain;

import com.technova.security.SenhaHasher;

public class Usuario {
    private int id;
    private String nome;
    private String email;
    private String senhaHash;
    private PerfilUsuario perfil;

    public Usuario() {
    }

    public Usuario(int id, String nome, String email, String senhaHash, PerfilUsuario perfil) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
    }

    public boolean verificarSenha(String senhaInformada) {
        return SenhaHasher.verificar(senhaInformada, senhaHash);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }
}
