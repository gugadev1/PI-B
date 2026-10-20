package com.technova.service;

import com.technova.domain.Cliente;
import com.technova.domain.ItemPedido;
import com.technova.domain.Pedido;
import com.technova.domain.Produto;
import com.technova.domain.StatusPedido;
import com.technova.domain.Usuario;
import com.technova.exception.RegraNegocioException;
import com.technova.exception.ValidacaoException;
import com.technova.repository.PedidoRepository;
import com.technova.repository.ProdutoRepository;
import com.technova.validation.PedidoValidator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servico de regras de negocio para Pedidos (RF004, RF005, RF006 e RF007).
 * Refatorado com Injecao de Dependencia (DIP), validacao dedicada (SRP) e controle de transicao de status.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoValidator pedidoValidator;

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository) {
        this(pedidoRepository, produtoRepository, new PedidoValidator());
    }

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository, PedidoValidator pedidoValidator) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.pedidoValidator = pedidoValidator;
    }

    public Pedido criarPedido(Cliente cliente, List<ItemPedido> itens) {
        pedidoValidator.validarCriacaoPedido(cliente, itens);

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        List<ItemPedido> debitados = new ArrayList<>();
        try {
            for (ItemPedido item : itens) {
                Produto produto = produtoRepository.buscarPorId(item.getProduto().getId())
                        .orElseThrow(() -> new RegraNegocioException("Produto nao encontrado."));
                if (!produto.isAtivo()) {
                    throw new RegraNegocioException("Produto inativo nao pode ser vendido: " + produto.getNome());
                }
                if (!produtoRepository.debitarEstoque(produto.getId(), item.getQuantidade())) {
                    throw new RegraNegocioException("Estoque insuficiente para o produto: " + produto.getNome());
                }
                debitados.add(new ItemPedido(produto, item.getQuantidade(), produto.getPreco()));
            }
            for (ItemPedido item : debitados) {
                pedido.adicionarItem(item);
            }
            return pedidoRepository.salvar(pedido);
        } catch (RuntimeException e) {
            for (ItemPedido item : debitados) {
                produtoRepository.estornarEstoque(item.getProduto().getId(), item.getQuantidade());
            }
            throw e;
        }
    }

    public void atualizarStatusPedido(Pedido pedido, StatusPedido novoStatus, Usuario usuarioResponsavel) {
        if (pedido == null || novoStatus == null) {
            throw new ValidacaoException("Pedido e novo status sao obrigatorios.");
        }
        if (usuarioResponsavel != null && !usuarioResponsavel.getPerfil().podeAlterarStatusPedido()) {
            throw new RegraNegocioException("Usuario sem permissao para alterar o status do pedido.");
        }

        StatusPedido statusAtual = pedido.getStatus();
        if (statusAtual == novoStatus) {
            return;
        }
        if (!statusAtual.podeTransicionarPara(novoStatus)) {
            throw new RegraNegocioException(
                    String.format("Transicao de status invalida: nao e permitido mudar de %s para %s.", statusAtual, novoStatus)
            );
        }

        if (pedido.getId() > 0
                && !pedidoRepository.atualizarStatus(pedido.getId(), statusAtual, novoStatus)) {
            throw new RegraNegocioException("O pedido foi alterado por outra operação; atualize e tente novamente.");
        }

        if (novoStatus == StatusPedido.CANCELADO) {
            estornarEstoqueItens(pedido);
        }

        pedido.setStatus(novoStatus);
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.listarTodos();
    }

    public List<Pedido> listarPorCliente(int idCliente) {
        return pedidoRepository.listarPorCliente(idCliente);
    }

    private void estornarEstoqueItens(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produtoRepository.estornarEstoque(produto.getId(), item.getQuantidade());
        }
    }
}
