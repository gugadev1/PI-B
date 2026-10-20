package com.technova.repository.memory;

import com.technova.domain.Produto;
import com.technova.repository.ProdutoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacao em memoria de ProdutoRepository.
 * Substitui a lista de produtos que antes ficava acoplada dentro de TechNovaDesktopUI.
 */
public class ProdutoRepositoryMemoria implements ProdutoRepository {
    private final List<Produto> produtos = new ArrayList<>();
    private int proximoId = 1;

    @Override
    public Produto salvar(Produto produto) {
        if (produto.getId() <= 0) {
            produto.setId(proximoId++);
        } else if (produto.getId() >= proximoId) {
            proximoId = produto.getId() + 1;
        }
        produtos.add(produto);
        return produto;
    }

    @Override
    public Produto atualizar(Produto produto) {
        Optional<Produto> existenteOpt = buscarPorId(produto.getId());
        if (existenteOpt.isPresent()) {
            Produto existente = existenteOpt.get();
            existente.setNome(produto.getNome());
            existente.setDescricao(produto.getDescricao());
            existente.setCategoria(produto.getCategoria());
            existente.setPreco(produto.getPreco());
            existente.setQuantidadeEstoque(produto.getQuantidadeEstoque());
            existente.setAtivo(produto.isAtivo());
            return existente;
        }
        return produto;
    }

    @Override
    public boolean remover(int id) {
        return produtos.removeIf(p -> p.getId() == id);
    }

    @Override
    public Optional<Produto> buscarPorId(int id) {
        return produtos.stream().filter(p -> p.getId() == id).findFirst();
    }

    @Override
    public List<Produto> listarTodos() {
        return new ArrayList<>(produtos);
    }

    @Override
    public boolean atualizarEstoque(int idProduto, int novaQuantidade) {
        Optional<Produto> produtoOpt = buscarPorId(idProduto);
        if (produtoOpt.isPresent() && novaQuantidade >= 0) {
            produtoOpt.get().setQuantidadeEstoque(novaQuantidade);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean debitarEstoque(int idProduto, int quantidade) {
        Optional<Produto> produtoOpt = buscarPorId(idProduto);
        if (quantidade <= 0 || produtoOpt.isEmpty()
                || !produtoOpt.get().possuiEstoqueSuficiente(quantidade)) {
            return false;
        }
        produtoOpt.get().debitarEstoque(quantidade);
        return true;
    }

    @Override
    public synchronized boolean estornarEstoque(int idProduto, int quantidade) {
        Optional<Produto> produtoOpt = buscarPorId(idProduto);
        if (quantidade <= 0 || produtoOpt.isEmpty()) {
            return false;
        }
        produtoOpt.get().estornarEstoque(quantidade);
        return true;
    }
}
