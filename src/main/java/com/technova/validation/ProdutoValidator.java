package com.technova.validation;

import com.technova.domain.Produto;
import com.technova.exception.ValidacaoException;

/**
 * Responsavel exclusivamente por validar os dados de um Produto (SRP).
 * Elimina a duplicacao de validacao entre a interface Swing e o ProdutoDAO.
 */
public class ProdutoValidator implements Validador<Produto> {

    @Override
    public void validar(Produto produto) {
        if (produto == null) {
            throw new ValidacaoException("Produto nao pode ser nulo.");
        }
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new ValidacaoException("Nome do produto e obrigatorio.");
        }
        if (produto.getCategoria() == null || produto.getCategoria().isBlank()) {
            throw new ValidacaoException("Categoria do produto e obrigatoria.");
        }
        if (!Double.isFinite(produto.getPreco()) || produto.getPreco() < 0) {
            throw new ValidacaoException("Preco do produto deve ser um numero finito nao negativo.");
        }
        if (produto.getQuantidadeEstoque() < 0) {
            throw new ValidacaoException("Quantidade em estoque nao pode ser negativa.");
        }
    }
}
