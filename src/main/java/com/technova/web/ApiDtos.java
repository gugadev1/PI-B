package com.technova.web;

import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record LoginInput(String email, String password) {
    }

    public record UsuarioOutput(int id, String name, String email, String role, Integer clientId) {
    }

    public record SessionOutput(UsuarioOutput user) {
    }

    public record ProdutoInput(String name, String description, String category,
            Double price, Integer stock) {
    }

    public record ProdutoOutput(int id, String name, String description, String category,
            double price, int stock, boolean active) {
    }

    public record ClienteInput(String name, String email, String password, String phone) {
    }

    public record ClienteOutput(int id, String name, String email, String phone) {
    }

    public record ItemPedidoInput(Integer productId, Integer quantity) {
    }

    public record PedidoInput(Integer clientId, List<ItemPedidoInput> items) {
    }

    public record StatusInput(String status) {
    }

    public record ItemPedidoOutput(int productId, String name, int quantity,
            double price, double subtotal) {
    }

    public record PedidoOutput(int id, int clientId, String clientName, LocalDateTime createdAt,
            String status, List<ItemPedidoOutput> items, double total) {
    }

    public record ErroOutput(String error) {
    }
}
