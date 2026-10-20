package com.technova.repository.jdbc;

import com.technova.domain.Produto;
import com.technova.repository.ProdutoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProdutoRepository implements ProdutoRepository {
    private final JdbcTechNovaRepository database;

    public JdbcProdutoRepository(JdbcTechNovaRepository database) {
        this.database = database;
    }

    @Override
    public Produto salvar(Produto produto) {
        return database.salvarProduto(produto);
    }

    @Override
    public Produto atualizar(Produto produto) {
        return database.atualizarProduto(produto);
    }

    @Override
    public boolean remover(int id) {
        return database.removerProduto(id);
    }

    @Override
    public Optional<Produto> buscarPorId(int id) {
        return database.buscarProdutoPorId(id);
    }

    @Override
    public List<Produto> listarTodos() {
        return database.listarProdutos();
    }

    @Override
    public boolean atualizarEstoque(int idProduto, int novaQuantidade) {
        return database.atualizarEstoque(idProduto, novaQuantidade);
    }

    @Override
    public boolean debitarEstoque(int idProduto, int quantidade) {
        return database.debitarEstoque(idProduto, quantidade);
    }

    @Override
    public boolean estornarEstoque(int idProduto, int quantidade) {
        return database.estornarEstoque(idProduto, quantidade);
    }
}
