package com.technova.repository;

import com.technova.domain.Produto;
import java.util.List;
import java.util.Optional;

/**
 * Interface de repositorio para persistencia de Produtos (DIP e ISP).
 */
public interface ProdutoRepository {
    Produto salvar(Produto produto);
    Produto atualizar(Produto produto);
    boolean remover(int id);
    Optional<Produto> buscarPorId(int id);
    List<Produto> listarTodos();
    boolean atualizarEstoque(int idProduto, int novaQuantidade);
}
