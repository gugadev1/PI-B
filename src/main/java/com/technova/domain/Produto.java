package com.technova.domain;

import com.technova.exception.RegraNegocioException;

/**
 * Entidade de dominio Produto enriquecida com metodos de controle de estoque
 * para evitar o code smell Feature Envy / Anemic Domain Model.
 */
public class Produto {
    private int id;
    private String nome;
    private String descricao;
    private String categoria;
    private double preco;
    private int quantidadeEstoque;
    private boolean ativo;

    public Produto() {
    }

    public Produto(int id, String nome, String descricao, String categoria, double preco, int quantidadeEstoque, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.ativo = ativo;
    }

    public boolean possuiEstoqueSuficiente(int quantidadeDesejada) {
        return quantidadeDesejada > 0 && this.quantidadeEstoque >= quantidadeDesejada;
    }

    public void debitarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade para baixa de estoque deve ser maior que zero.");
        }
        if (!possuiEstoqueSuficiente(quantidade)) {
            throw new RegraNegocioException("Estoque insuficiente para o produto: " + this.nome);
        }
        this.quantidadeEstoque -= quantidade;
    }

    public void estornarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade para estorno de estoque deve ser maior que zero.");
        }
        this.quantidadeEstoque += quantidade;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
