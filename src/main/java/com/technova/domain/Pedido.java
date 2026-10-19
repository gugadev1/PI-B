package com.technova.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {
    private int id;
    private Cliente cliente;
    private LocalDateTime dataCriacao;
    private StatusPedido status;
    private final List<ItemPedido> itens;
    private double total;

    public Pedido() {
        this.itens = new ArrayList<>();
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusPedido.EM_PROCESSAMENTO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        if (dataCriacao != null) {
            this.dataCriacao = dataCriacao;
        }
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = Math.max(total, 0.0);
    }

    public void adicionarItem(ItemPedido item) {
        if (item != null) {
            this.itens.add(item);
            recalcularTotal();
        }
    }

    public void recalcularTotal() {
        double soma = 0.0;
        for (ItemPedido item : itens) {
            soma += item.getSubtotal();
        }
        this.total = soma;
    }
}
