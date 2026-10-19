package com.technova.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.technova.domain.Produto;
import com.technova.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

class CarrinhoServiceTest {

    @Test
    void deveConsolidarProdutoECalcularTotalDoCarrinho() {
        CarrinhoService carrinho = new CarrinhoService();
        Produto produto = new Produto(1, "Teclado", "Mecanico", "Perifericos", 150.50, 5, true);

        carrinho.adicionarItem(produto, 2);
        carrinho.adicionarItem(produto, 1);

        assertEquals(1, carrinho.listarItens().size());
        assertEquals(3, carrinho.listarItens().get(0).getQuantidade());
        assertEquals(451.50, carrinho.calcularTotal(), 0.001);
    }

    @Test
    void deveRejeitarQuantidadeAcimaDoEstoqueSemAlterarOCarrinho() {
        CarrinhoService carrinho = new CarrinhoService();
        Produto produto = new Produto(1, "Mouse", "Sem fio", "Perifericos", 80.00, 2, true);

        carrinho.adicionarItem(produto, 1);

        assertThrows(RegraNegocioException.class, () -> carrinho.adicionarItem(produto, 2));
        assertEquals(1, carrinho.listarItens().size());
        assertEquals(1, carrinho.listarItens().get(0).getQuantidade());
        assertEquals(80.00, carrinho.calcularTotal(), 0.001);
    }
}
