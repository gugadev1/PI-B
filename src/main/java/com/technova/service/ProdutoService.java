package com.technova.service;

import com.technova.domain.Produto;
import com.technova.exception.RecursoNaoEncontradoException;
import com.technova.repository.ProdutoRepository;
import com.technova.validation.ProdutoValidator;
import com.technova.validation.Validador;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servico de regras de negocio para gerenciamento de Produtos (RF002).
 * Extraido da classe TechNovaDesktopUI aplicando SRP, OCP e DIP.
 */
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final Validador<Produto> validador;

    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository) {
        this(produtoRepository, new ProdutoValidator());
    }

    public ProdutoService(ProdutoRepository produtoRepository, Validador<Produto> validador) {
        this.produtoRepository = produtoRepository;
        this.validador = validador;
    }

    public Produto cadastrarProduto(Produto produto) {
        validador.validar(produto);
        return produtoRepository.salvar(produto);
    }

    public Produto atualizarProduto(Produto produto) {
        validador.validar(produto);
        if (produto.getId() <= 0 || produtoRepository.buscarPorId(produto.getId()).isEmpty()) {
            throw new RecursoNaoEncontradoException("Produto não encontrado para atualização.");
        }
        return produtoRepository.atualizar(produto);
    }

    public void removerProduto(int idProduto) {
        if (!produtoRepository.remover(idProduto)) {
            throw new RecursoNaoEncontradoException("Produto não encontrado para remoção: ID " + idProduto);
        }
    }

    public Produto buscarPorId(int idProduto) {
        return produtoRepository.buscarPorId(idProduto)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: ID " + idProduto));
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.listarTodos();
    }
}
