package com.technova.ui;

import com.technova.domain.Cliente;
import com.technova.domain.ItemPedido;
import com.technova.domain.Pedido;
import com.technova.domain.Produto;
import com.technova.domain.StatusPedido;
import com.technova.domain.Usuario;
import com.technova.exception.RegraNegocioException;
import com.technova.service.AutenticacaoService;
import com.technova.service.CarrinhoService;
import com.technova.service.PedidoService;
import com.technova.service.ProdutoService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Camada de apresentacao Java Swing refatorada (SRP e DIP).
 * Nao contem listas de dados em memoria nem regras de negocio: delega todas as operacoes
 * para AutenticacaoService, ProdutoService, CarrinhoService e PedidoService.
 */
public class TechNovaDesktopUI extends JFrame {

    private static final String TELA_LOGIN = "login";
    private static final String TELA_PRODUTOS = "produtos";
    private static final String TELA_PEDIDOS = "pedidos";

    private final CardLayout cardLayout;
    private final JPanel conteudo;

    private final AutenticacaoService autenticacaoService;
    private final ProdutoService produtoService;
    private final CarrinhoService carrinhoService;
    private final PedidoService pedidoService;
    private final Cliente clientePadrao;

    private JTextField campoEmailLogin;
    private JPasswordField campoSenhaLogin;

    private JLabel labelUsuarioProdutos;
    private JTable tabelaProdutos;
    private DefaultTableModel modeloProdutos;
    private JTextField campoNomeProduto;
    private JTextField campoDescricaoProduto;
    private JTextField campoCategoriaProduto;
    private JTextField campoPrecoProduto;
    private JTextField campoEstoqueProduto;
    private JCheckBox checkAtivoProduto;

    private JLabel labelUsuarioPedidos;
    private JComboBox<String> comboProdutosPedido;
    private JSpinner spinnerQuantidade;
    private JTable tabelaCarrinho;
    private DefaultTableModel modeloCarrinho;
    private JLabel labelTotalCarrinho;
    private JTable tabelaPedidos;
    private DefaultTableModel modeloPedidos;
    private JComboBox<StatusPedido> comboStatusPedido;

    public TechNovaDesktopUI(
            AutenticacaoService autenticacaoService,
            ProdutoService produtoService,
            CarrinhoService carrinhoService,
            PedidoService pedidoService,
            Cliente clientePadrao) {

        this.autenticacaoService = autenticacaoService;
        this.produtoService = produtoService;
        this.carrinhoService = carrinhoService;
        this.pedidoService = pedidoService;
        this.clientePadrao = clientePadrao;

        this.cardLayout = new CardLayout();
        this.conteudo = new JPanel(cardLayout);

        configurarJanela();
        montarTelas();
        atualizarDadosVisuais();
    }

    private void configurarJanela() {
        setTitle("TechNova Solucoes Desktop - Refatorado (SOLID)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setContentPane(conteudo);
    }

    private void montarTelas() {
        conteudo.add(criarTelaLogin(), TELA_LOGIN);
        conteudo.add(criarTelaProdutos(), TELA_PRODUTOS);
        conteudo.add(criarTelaPedidos(), TELA_PEDIDOS);
        cardLayout.show(conteudo, TELA_LOGIN);
    }

    private JPanel criarTelaLogin() {
        JPanel painel = new JPanel(new BorderLayout(16, 16));
        painel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JLabel titulo = new JLabel("TechNova - Login", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        painel.add(titulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridLayout(0, 1, 10, 10));
        formulario.setBorder(BorderFactory.createEmptyBorder(80, 280, 80, 280));

        formulario.add(new JLabel("Email:"));
        campoEmailLogin = new JTextField();
        formulario.add(campoEmailLogin);

        formulario.add(new JLabel("Senha:"));
        campoSenhaLogin = new JPasswordField();
        formulario.add(campoSenhaLogin);

        JButton botaoEntrar = new JButton("Entrar");
        botaoEntrar.addActionListener(e -> autenticarUsuario());
        formulario.add(botaoEntrar);

        JLabel ajuda = new JLabel("Use um usuario cadastrado para acessar o sistema.", SwingConstants.CENTER);
        formulario.add(ajuda);

        painel.add(formulario, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarTelaProdutos() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topo = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("TechNova - Produtos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topo.add(titulo, BorderLayout.WEST);

        JPanel acoesTopo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        labelUsuarioProdutos = new JLabel();
        JButton botaoPedidos = new JButton("Ir para Pedidos");
        botaoPedidos.addActionListener(e -> cardLayout.show(conteudo, TELA_PEDIDOS));
        JButton botaoSair = new JButton("Sair");
        botaoSair.addActionListener(e -> logout());

        acoesTopo.add(labelUsuarioProdutos);
        acoesTopo.add(botaoPedidos);
        acoesTopo.add(botaoSair);
        topo.add(acoesTopo, BorderLayout.EAST);

        painel.add(topo, BorderLayout.NORTH);

        modeloProdutos = new DefaultTableModel(new Object[]{"ID", "Nome", "Categoria", "Preco", "Estoque", "Ativo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaProdutos = new JTable(modeloProdutos);
        tabelaProdutos.getSelectionModel().addListSelectionListener(e -> preencherFormularioProdutoPorSelecao());
        painel.add(new JScrollPane(tabelaProdutos), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridLayout(0, 4, 8, 8));
        formulario.setBorder(BorderFactory.createTitledBorder("Cadastro e Edicao de Produto"));

        campoNomeProduto = new JTextField();
        campoDescricaoProduto = new JTextField();
        campoCategoriaProduto = new JTextField();
        campoPrecoProduto = new JTextField();
        campoEstoqueProduto = new JTextField();
        checkAtivoProduto = new JCheckBox("Ativo", true);

        formulario.add(new JLabel("Nome"));
        formulario.add(campoNomeProduto);
        formulario.add(new JLabel("Descricao"));
        formulario.add(campoDescricaoProduto);
        formulario.add(new JLabel("Categoria"));
        formulario.add(campoCategoriaProduto);
        formulario.add(new JLabel("Preco (R$)"));
        formulario.add(campoPrecoProduto);
        formulario.add(new JLabel("Estoque"));
        formulario.add(campoEstoqueProduto);
        formulario.add(new JLabel("Status"));
        formulario.add(checkAtivoProduto);

        JButton botaoAdicionar = new JButton("Adicionar");
        botaoAdicionar.addActionListener(e -> adicionarProduto());
        JButton botaoAtualizar = new JButton("Atualizar Selecionado");
        botaoAtualizar.addActionListener(e -> atualizarProdutoSelecionado());
        JButton botaoRemover = new JButton("Remover Selecionado");
        botaoRemover.addActionListener(e -> removerProdutoSelecionado());
        JButton botaoLimpar = new JButton("Limpar Campos");
        botaoLimpar.addActionListener(e -> limparFormularioProduto());

        formulario.add(botaoAdicionar);
        formulario.add(botaoAtualizar);
        formulario.add(botaoRemover);
        formulario.add(botaoLimpar);

        painel.add(formulario, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarTelaPedidos() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topo = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("TechNova - Pedidos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topo.add(titulo, BorderLayout.WEST);

        JPanel acoesTopo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        labelUsuarioPedidos = new JLabel();
        JButton botaoProdutos = new JButton("Ir para Produtos");
        botaoProdutos.addActionListener(e -> cardLayout.show(conteudo, TELA_PRODUTOS));
        JButton botaoSair = new JButton("Sair");
        botaoSair.addActionListener(e -> logout());
        acoesTopo.add(labelUsuarioPedidos);
        acoesTopo.add(botaoProdutos);
        acoesTopo.add(botaoSair);
        topo.add(acoesTopo, BorderLayout.EAST);
        painel.add(topo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 8, 8));

        JPanel painelCriacao = new JPanel(new BorderLayout(8, 8));
        painelCriacao.setBorder(BorderFactory.createTitledBorder("Novo Pedido"));

        JPanel linhaSelecao = new JPanel(new GridLayout(0, 2, 8, 8));
        comboProdutosPedido = new JComboBox<>();
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

        linhaSelecao.add(new JLabel("Produto"));
        linhaSelecao.add(comboProdutosPedido);
        linhaSelecao.add(new JLabel("Quantidade"));
        linhaSelecao.add(spinnerQuantidade);

        JButton botaoAdicionarItem = new JButton("Adicionar item ao carrinho");
        botaoAdicionarItem.addActionListener(e -> adicionarItemCarrinho());
        linhaSelecao.add(botaoAdicionarItem);

        JButton botaoRemoverItem = new JButton("Remover item selecionado");
        botaoRemoverItem.addActionListener(e -> removerItemCarrinhoSelecionado());
        linhaSelecao.add(botaoRemoverItem);

        painelCriacao.add(linhaSelecao, BorderLayout.NORTH);

        modeloCarrinho = new DefaultTableModel(new Object[]{"Produto", "Qtd", "Preco Unit.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaCarrinho = new JTable(modeloCarrinho);
        painelCriacao.add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);

        JPanel rodapeCarrinho = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        labelTotalCarrinho = new JLabel("Total: R$ 0,00");
        JButton botaoFinalizar = new JButton("Finalizar Pedido");
        botaoFinalizar.addActionListener(e -> finalizarPedido());
        rodapeCarrinho.add(labelTotalCarrinho);
        rodapeCarrinho.add(botaoFinalizar);
        painelCriacao.add(rodapeCarrinho, BorderLayout.SOUTH);

        centro.add(painelCriacao);

        JPanel painelHistorico = new JPanel(new BorderLayout(8, 8));
        painelHistorico.setBorder(BorderFactory.createTitledBorder("Pedidos Realizados"));

        modeloPedidos = new DefaultTableModel(new Object[]{"ID", "Data", "Status", "Total", "Itens"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaPedidos = new JTable(modeloPedidos);
        painelHistorico.add(new JScrollPane(tabelaPedidos), BorderLayout.CENTER);

        JPanel alteracaoStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        comboStatusPedido = new JComboBox<>(StatusPedido.values());
        JButton botaoAtualizarStatus = new JButton("Atualizar Status");
        botaoAtualizarStatus.addActionListener(e -> atualizarStatusPedidoSelecionado());
        alteracaoStatus.add(new JLabel("Novo status:"));
        alteracaoStatus.add(comboStatusPedido);
        alteracaoStatus.add(botaoAtualizarStatus);
        painelHistorico.add(alteracaoStatus, BorderLayout.SOUTH);

        centro.add(painelHistorico);
        painel.add(centro, BorderLayout.CENTER);
        return painel;
    }

    private void autenticarUsuario() {
        String email = campoEmailLogin.getText();
        String senha = new String(campoSenhaLogin.getPassword());

        try {
            autenticacaoService.autenticar(email, senha);
            campoSenhaLogin.setText("");
            atualizarDadosVisuais();
            cardLayout.show(conteudo, TELA_PRODUTOS);
        } catch (RegraNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Login", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void logout() {
        autenticacaoService.encerrarSessao();
        campoEmailLogin.setText("");
        campoSenhaLogin.setText("");
        cardLayout.show(conteudo, TELA_LOGIN);
    }

    private void adicionarProduto() {
        try {
            Produto novo = lerProdutoDoFormulario(0);
            produtoService.cadastrarProduto(novo);
            atualizarDadosVisuais();
            limparFormularioProduto();
            JOptionPane.showMessageDialog(this, "Produto adicionado com sucesso.");
        } catch (RegraNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Produto", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void atualizarProdutoSelecionado() {
        int linha = tabelaProdutos.getSelectedRow();
        List<Produto> produtos = produtoService.listarProdutos();
        if (linha < 0 || linha >= produtos.size()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para atualizar.", "Produto", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idExistente = produtos.get(linha).getId();
            Produto atualizado = lerProdutoDoFormulario(idExistente);
            produtoService.atualizarProduto(atualizado);
            atualizarDadosVisuais();
            JOptionPane.showMessageDialog(this, "Produto atualizado com sucesso.");
        } catch (RegraNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Produto", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removerProdutoSelecionado() {
        int linha = tabelaProdutos.getSelectedRow();
        List<Produto> produtos = produtoService.listarProdutos();
        if (linha < 0 || linha >= produtos.size()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para remover.", "Produto", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            produtoService.removerProduto(produtos.get(linha).getId());
            atualizarDadosVisuais();
            limparFormularioProduto();
        } catch (RegraNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Produto", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Produto lerProdutoDoFormulario(int id) {
        String nome = campoNomeProduto.getText().trim();
        String descricao = campoDescricaoProduto.getText().trim();
        String categoria = campoCategoriaProduto.getText().trim();
        String precoTexto = campoPrecoProduto.getText().trim().replace(',', '.');
        String estoqueTexto = campoEstoqueProduto.getText().trim();

        try {
            double preco = Double.parseDouble(precoTexto);
            int estoque = Integer.parseInt(estoqueTexto);
            return new Produto(id, nome, descricao, categoria, preco, estoque, checkAtivoProduto.isSelected());
        } catch (NumberFormatException e) {
            throw new RegraNegocioException("Preco e estoque devem ser numericos.");
        }
    }

    private void preencherFormularioProdutoPorSelecao() {
        int linha = tabelaProdutos.getSelectedRow();
        List<Produto> produtos = produtoService.listarProdutos();
        if (linha < 0 || linha >= produtos.size()) {
            return;
        }
        Produto p = produtos.get(linha);
        campoNomeProduto.setText(p.getNome());
        campoDescricaoProduto.setText(p.getDescricao());
        campoCategoriaProduto.setText(p.getCategoria());
        campoPrecoProduto.setText(String.format("%.2f", p.getPreco()));
        campoEstoqueProduto.setText(String.valueOf(p.getQuantidadeEstoque()));
        checkAtivoProduto.setSelected(p.isAtivo());
    }

    private void limparFormularioProduto() {
        campoNomeProduto.setText("");
        campoDescricaoProduto.setText("");
        campoCategoriaProduto.setText("");
        campoPrecoProduto.setText("");
        campoEstoqueProduto.setText("");
        checkAtivoProduto.setSelected(true);
        tabelaProdutos.clearSelection();
    }

    private void adicionarItemCarrinho() {
        int indice = comboProdutosPedido.getSelectedIndex();
        List<Produto> produtos = produtoService.listarProdutos();
        if (indice < 0 || indice >= produtos.size()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto valido.", "Pedido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Produto produto = produtos.get(indice);
            int quantidade = (Integer) spinnerQuantidade.getValue();
            carrinhoService.adicionarItem(produto, quantidade);
            atualizarTabelaCarrinho();
        } catch (RegraNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Pedido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removerItemCarrinhoSelecionado() {
        int linha = tabelaCarrinho.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um item para remover.", "Pedido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            carrinhoService.removerItemPorIndice(linha);
            atualizarTabelaCarrinho();
        } catch (RegraNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Pedido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void finalizarPedido() {
        if (carrinhoService.estaVazio()) {
            JOptionPane.showMessageDialog(this, "Adicione ao menos um item ao pedido.", "Pedido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            pedidoService.criarPedido(clientePadrao, carrinhoService.listarItens());
            carrinhoService.limpar();
            atualizarDadosVisuais();
            JOptionPane.showMessageDialog(this, "Pedido finalizado com sucesso.");
        } catch (RegraNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Pedido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarStatusPedidoSelecionado() {
        int linha = tabelaPedidos.getSelectedRow();
        List<Pedido> pedidos = pedidoService.listarTodos();
        if (linha < 0 || linha >= pedidos.size()) {
            JOptionPane.showMessageDialog(this, "Selecione um pedido para atualizar status.", "Pedido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StatusPedido novoStatus = (StatusPedido) comboStatusPedido.getSelectedItem();
        if (novoStatus == null) {
            return;
        }

        try {
            Pedido pedido = pedidos.get(linha);
            pedidoService.atualizarStatusPedido(pedido, novoStatus, autenticacaoService.getUsuarioAutenticado());
            atualizarDadosVisuais();
        } catch (RegraNegocioException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Pedido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void atualizarDadosVisuais() {
        Usuario usuarioLogado = autenticacaoService.getUsuarioAutenticado();
        String usuarioTexto = usuarioLogado == null
                ? "Nao autenticado"
                : "Usuario: " + usuarioLogado.getNome() + " (" + usuarioLogado.getPerfil() + ")";

        labelUsuarioProdutos.setText(usuarioTexto);
        labelUsuarioPedidos.setText(usuarioTexto);

        atualizarTabelaProdutos();
        atualizarComboProdutos();
        atualizarTabelaCarrinho();
        atualizarTabelaPedidos();
    }

    private void atualizarTabelaProdutos() {
        modeloProdutos.setRowCount(0);
        for (Produto produto : produtoService.listarProdutos()) {
            modeloProdutos.addRow(new Object[]{
                produto.getId(),
                produto.getNome(),
                produto.getCategoria(),
                String.format("%.2f", produto.getPreco()),
                produto.getQuantidadeEstoque(),
                produto.isAtivo() ? "SIM" : "NAO"
            });
        }
    }

    private void atualizarComboProdutos() {
        comboProdutosPedido.removeAllItems();
        for (Produto produto : produtoService.listarProdutos()) {
            comboProdutosPedido.addItem(String.format("%d - %s | R$ %.2f | Estoque: %d",
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                produto.getQuantidadeEstoque()));
        }
    }

    private void atualizarTabelaCarrinho() {
        modeloCarrinho.setRowCount(0);
        for (ItemPedido item : carrinhoService.listarItens()) {
            modeloCarrinho.addRow(new Object[]{
                item.getProduto().getNome(),
                item.getQuantidade(),
                String.format("%.2f", item.getPrecoUnitario()),
                String.format("%.2f", item.getSubtotal())
            });
        }
        labelTotalCarrinho.setText(String.format("Total: R$ %.2f", carrinhoService.calcularTotal()));
    }

    private void atualizarTabelaPedidos() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        modeloPedidos.setRowCount(0);
        for (Pedido pedido : pedidoService.listarTodos()) {
            modeloPedidos.addRow(new Object[]{
                pedido.getId(),
                pedido.getDataCriacao().format(formatter),
                pedido.getStatus(),
                String.format("%.2f", pedido.getTotal()),
                pedido.getItens().size()
            });
        }
    }
}
