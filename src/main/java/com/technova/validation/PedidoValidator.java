package com.technova.validation;

import com.technova.domain.Cliente;
import com.technova.domain.ItemPedido;
import com.technova.domain.Produto;
import com.technova.exception.RegraNegocioException;
import com.technova.exception.ValidacaoException;
import java.util.List;

/**
 * Responsavel por validar os dados e o estoque dos itens de um Pedido (SRP).
 */
public class PedidoValidator {

    public void validarCriacaoPedido(Cliente cliente, List<ItemPedido> itens) {
        if (cliente == null) {
            throw new ValidacaoException("Cliente obrigatorio para criar pedido.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new ValidacaoException("Pedido deve conter ao menos um item.");
        }

        double total = 0;
        for (ItemPedido item : itens) {
            validarItem(item);
            total += item.getSubtotal();
            if (!Double.isFinite(total) || total > 9_999_999_999.99) {
                throw new ValidacaoException("O total do pedido excede o limite permitido.");
            }
        }
    }

    public void validarItem(ItemPedido item) {
        if (item == null || item.getProduto() == null) {
            throw new ValidacaoException("Item do pedido possui produto invalido.");
        }
        Produto produto = item.getProduto();
        if (!produto.isAtivo()) {
            throw new RegraNegocioException("Produto inativo nao pode ser vendido: " + produto.getNome());
        }
        if (item.getQuantidade() <= 0) {
            throw new ValidacaoException("Quantidade do item deve ser maior que zero.");
        }
        if (!Double.isFinite(item.getPrecoUnitario()) || item.getPrecoUnitario() < 0
                || !Double.isFinite(item.getSubtotal())) {
            throw new ValidacaoException("Preco unitario do item deve ser finito e nao negativo.");
        }
        if (!produto.possuiEstoqueSuficiente(item.getQuantidade())) {
            throw new RegraNegocioException("Estoque insuficiente para o produto: " + produto.getNome());
        }
    }
}
