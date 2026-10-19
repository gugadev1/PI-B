package com.technova.service;

import com.technova.domain.ItemPedido;
import com.technova.domain.Produto;
import com.technova.exception.RegraNegocioException;
import com.technova.exception.ValidacaoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Servico que encapsula a logica de negocio do carrinho de compras (SRP).
 * Extraido de TechNovaDesktopUI (adicionarItemCarrinho, removerItemCarrinhoSelecionado e calculo de total),
 * permitindo uso identico tanto na tela Swing quanto em uma futura sessao HTTP Web.
 */
public class CarrinhoService {

    private final List<ItemPedido> itens = new ArrayList<>();

    public void adicionarItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new ValidacaoException("Selecione um produto valido.");
        }
        if (!produto.isAtivo()) {
            throw new RegraNegocioException("Produto inativo nao pode ser adicionado ao carrinho: " + produto.getNome());
        }
        if (quantidade <= 0) {
            throw new ValidacaoException("Quantidade deve ser maior que zero.");
        }

        for (ItemPedido itemExistente : itens) {
            if (itemExistente.getProduto().getId() == produto.getId()) {
                int novaQuantidadeTotal = itemExistente.getQuantidade() + quantidade;
                if (!produto.possuiEstoqueSuficiente(novaQuantidadeTotal)) {
                    throw new RegraNegocioException("Estoque insuficiente para adicionar mais unidades de: " + produto.getNome());
                }
                itemExistente.setQuantidade(novaQuantidadeTotal);
                return;
            }
        }

        if (!produto.possuiEstoqueSuficiente(quantidade)) {
            throw new RegraNegocioException("Estoque insuficiente para o produto: " + produto.getNome());
        }

        itens.add(new ItemPedido(produto, quantidade, produto.getPreco()));
    }

    public void removerItemPorIndice(int indice) {
        if (indice < 0 || indice >= itens.size()) {
            throw new ValidacaoException("Indice de item invalido para remocao do carrinho.");
        }
        itens.remove(indice);
    }

    public void limpar() {
        itens.clear();
    }

    public boolean estaVazio() {
        return itens.isEmpty();
    }

    public List<ItemPedido> listarItens() {
        return Collections.unmodifiableList(itens);
    }

    public double calcularTotal() {
        double total = 0.0;
        for (ItemPedido item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }
}
