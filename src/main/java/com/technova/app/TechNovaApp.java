package com.technova.app;

import com.technova.domain.Cliente;
import com.technova.domain.Pedido;
import com.technova.domain.PerfilUsuario;
import com.technova.domain.Produto;
import com.technova.domain.StatusPedido;
import com.technova.domain.Usuario;
import com.technova.exception.RegraNegocioException;
import com.technova.exception.ValidacaoException;
import com.technova.repository.ClienteRepository;
import com.technova.repository.PedidoRepository;
import com.technova.repository.ProdutoRepository;
import com.technova.repository.UsuarioRepository;
import com.technova.repository.memory.ClienteRepositoryMemoria;
import com.technova.repository.memory.PedidoRepositoryMemoria;
import com.technova.repository.memory.ProdutoRepositoryMemoria;
import com.technova.repository.memory.UsuarioRepositoryMemoria;
import com.technova.service.AutenticacaoService;
import com.technova.service.CarrinhoService;
import com.technova.service.ClienteService;
import com.technova.service.PedidoService;
import com.technova.service.ProdutoService;
import com.technova.ui.TechNovaDesktopUI;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Classe principal do projeto TechNova Refatorado (PI2).
 * Executa os testes automatizados das regras de negocio no metodo main() sem depender do Java Swing,
 * comprovando o desacoplamento e a prontidao das classes para reutilizacao no sistema Web.
 */
public class TechNovaApp {

    private static int testesExecutados = 0;
    private static int testesAprovados = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  TECHNOVA SOLUCOES - SUITE DE TESTES DE REGRAS DE NEGOCIO (SOLID)    ");
        System.out.println("======================================================================");

        // Instanciacao dos repositorios (Inversao de Dependencia - DIP)
        UsuarioRepository usuarioRepo = new UsuarioRepositoryMemoria();
        ProdutoRepository produtoRepo = new ProdutoRepositoryMemoria();
        ClienteRepository clienteRepo = new ClienteRepositoryMemoria();
        PedidoRepository pedidoRepo = new PedidoRepositoryMemoria();

        // Instanciacao dos servicos de negocio desacoplados da UI (SRP / DIP)
        AutenticacaoService autenticacaoService = new AutenticacaoService(usuarioRepo);
        ProdutoService produtoService = new ProdutoService(produtoRepo);
        ClienteService clienteService = new ClienteService(clienteRepo);
        CarrinhoService carrinhoService = new CarrinhoService();
        PedidoService pedidoService = new PedidoService(pedidoRepo, produtoRepo);

        popularDadosIniciais(usuarioRepo, produtoService, clienteService);

        executarTestesAutenticacao(autenticacaoService);
        executarTestesProduto(produtoService);
        executarTestesCliente(clienteService);
        executarTestesCarrinhoEPedido(produtoService, clienteService, carrinhoService, pedidoService, autenticacaoService);

        System.out.println("----------------------------------------------------------------------");
        System.out.printf("RESULTADO FINAL: %d/%d testes aprovados com sucesso!%n", testesAprovados, testesExecutados);
        System.out.println("======================================================================");

        // Caso o argumento --ui seja informado, abre tambem a interface Desktop refatorada
        if (args.length > 0 && "--ui".equalsIgnoreCase(args[0])) {
            iniciarInterfaceDesktop(autenticacaoService, produtoService, carrinhoService, pedidoService,
                    clienteService.buscarPorId(1));
        }
    }

    private static void popularDadosIniciais(
            UsuarioRepository usuarioRepo,
            ProdutoService produtoService,
            ClienteService clienteService) {

        usuarioRepo.salvar(new Usuario(1, "Administrador Padrao", "admin@technova.com", "admin123", PerfilUsuario.ADMINISTRADOR));
        usuarioRepo.salvar(new Usuario(2, "Vendedor Padrao", "vendas@technova.com", "vendas123", PerfilUsuario.FUNCIONARIO));

        produtoService.cadastrarProduto(new Produto(1, "RTX 4060", "Placa de video 8GB", "Hardware", 2299.90, 5, true));
        produtoService.cadastrarProduto(new Produto(2, "Teclado Mecanico", "Switch blue ABNT2", "Perifericos", 299.90, 10, true));
        produtoService.cadastrarProduto(new Produto(3, "SSD NVMe 1TB", "Leitura 3500MB/s", "Armazenamento", 489.90, 3, true));

        clienteService.cadastrarCliente(new Cliente(1, "Cliente Balcao", "cliente.balcao@technova.com", "123456", "(51) 99999-0000"));
    }

    private static void executarTestesAutenticacao(AutenticacaoService autenticacaoService) {
        System.out.println("\n[1] Testando AutenticacaoService (RF001)...");

        Usuario admin = autenticacaoService.autenticar("admin@technova.com", "admin123");
        afirmar(admin != null && admin.getPerfil() == PerfilUsuario.ADMINISTRADOR,
                "Deve autenticar Administrador com credenciais validas");

        boolean bloqueouSenhaIncorreta = false;
        try {
            autenticacaoService.autenticar("admin@technova.com", "senhaErrada");
        } catch (RegraNegocioException e) {
            bloqueouSenhaIncorreta = true;
        }
        afirmar(bloqueouSenhaIncorreta, "Deve rejeitar login com senha incorreta");

        boolean bloqueouCamposVazios = false;
        try {
            autenticacaoService.autenticar("", "");
        } catch (ValidacaoException e) {
            bloqueouCamposVazios = true;
        }
        afirmar(bloqueouCamposVazios, "Deve validar campos obrigatorios de email e senha");
    }

    private static void executarTestesProduto(ProdutoService produtoService) {
        System.out.println("\n[2] Testando ProdutoService e ProdutoValidator (RF002)...");

        Produto monitor = new Produto(0, "Monitor Gamer 27", "144Hz IPS", "Monitores", 1199.90, 8, true);
        Produto salvo = produtoService.cadastrarProduto(monitor);
        afirmar(salvo.getId() == 4 && produtoService.listarProdutos().size() == 4,
                "Deve cadastrar novo produto e atribuir ID sequencial");

        boolean bloqueouPrecoNegativo = false;
        try {
            produtoService.cadastrarProduto(new Produto(0, "Mouse", "Optico", "Perifericos", -50.0, 5, true));
        } catch (ValidacaoException e) {
            bloqueouPrecoNegativo = true;
        }
        afirmar(bloqueouPrecoNegativo, "Deve impedir cadastro de produto com preco negativo");

        salvo.setPreco(1099.90);
        produtoService.atualizarProduto(salvo);
        afirmar(Math.abs(produtoService.buscarPorId(4).getPreco() - 1099.90) < 0.001,
                "Deve atualizar dados de produto existente");
    }

    private static void executarTestesCliente(ClienteService clienteService) {
        System.out.println("\n[3] Testando ClienteService e ClienteValidator (RF003)...");

        Cliente novoCliente = new Cliente(0, "Maria Silva", "maria.silva@email.com", "hash123", "(51) 98888-7777");
        Cliente clienteSalvo = clienteService.cadastrarCliente(novoCliente);
        afirmar(clienteSalvo.getId() == 2 && clienteService.listarClientes().size() == 2,
                "Deve cadastrar cliente valido com sucesso");

        boolean bloqueouEmailInvalido = false;
        try {
            clienteService.cadastrarCliente(new Cliente(0, "Cliente Invalido", "email-sem-arroba", "123", "(51) 90000-0000"));
        } catch (ValidacaoException e) {
            bloqueouEmailInvalido = true;
        }
        afirmar(bloqueouEmailInvalido, "Deve impedir cadastro de cliente com email invalido");
    }

    private static void executarTestesCarrinhoEPedido(
            ProdutoService produtoService,
            ClienteService clienteService,
            CarrinhoService carrinhoService,
            PedidoService pedidoService,
            AutenticacaoService autenticacaoService) {

        System.out.println("\n[4] Testando CarrinhoService e PedidoService (RF004, RF005, RF006 e RF007)...");

        Produto rtx = produtoService.buscarPorId(1); // Estoque inicial: 5
        Produto ssd = produtoService.buscarPorId(3); // Estoque inicial: 3

        carrinhoService.adicionarItem(rtx, 1);
        carrinhoService.adicionarItem(rtx, 1); // Deve somar quantidade = 2
        carrinhoService.adicionarItem(ssd, 2);

        afirmar(carrinhoService.listarItens().size() == 2,
                "Deve consolidar itens do mesmo produto no carrinho");

        double totalEsperado = (2 * 2299.90) + (2 * 489.90);
        afirmar(Math.abs(carrinhoService.calcularTotal() - totalEsperado) < 0.01,
                "Deve calcular o valor total do carrinho corretamente (R$ " + String.format("%.2f", totalEsperado) + ")");

        boolean bloqueouExcessoEstoque = false;
        try {
            carrinhoService.adicionarItem(ssd, 5); // Estoque total e 3, ja tem 2 no carrinho
        } catch (RegraNegocioException e) {
            bloqueouExcessoEstoque = true;
        }
        afirmar(bloqueouExcessoEstoque, "Deve impedir adicao no carrinho acima do estoque disponivel");

        Cliente cliente = clienteService.buscarPorId(1);
        Pedido pedido = pedidoService.criarPedido(cliente, carrinhoService.listarItens());
        carrinhoService.limpar();

        afirmar(pedido.getId() == 1 && pedido.getStatus() == StatusPedido.EM_PROCESSAMENTO,
                "Deve criar pedido com status inicial EM_PROCESSAMENTO");
        afirmar(rtx.getQuantidadeEstoque() == 3 && ssd.getQuantidadeEstoque() == 1,
                "Deve baixar automaticamente o estoque dos produtos apos confirmar o pedido");

        // Testando permissao e fluxo de status
        Usuario admin = autenticacaoService.getUsuarioAutenticado();
        pedidoService.atualizarStatusPedido(pedido, StatusPedido.APROVADO, admin);
        afirmar(pedido.getStatus() == StatusPedido.APROVADO,
                "Deve permitir transicao de EM_PROCESSAMENTO para APROVADO por Administrador");

        boolean bloqueouAlteracaoPorCliente = false;
        try {
            pedidoService.atualizarStatusPedido(pedido, StatusPedido.ENVIADO, cliente);
        } catch (RegraNegocioException e) {
            bloqueouAlteracaoPorCliente = true;
        }
        afirmar(bloqueouAlteracaoPorCliente,
                "Deve bloquear alteracao de status de pedido por usuario com perfil CLIENTE");

        // Testando cancelamento e estorno de estoque
        pedidoService.atualizarStatusPedido(pedido, StatusPedido.CANCELADO, admin);
        afirmar(pedido.getStatus() == StatusPedido.CANCELADO && rtx.getQuantidadeEstoque() == 5 && ssd.getQuantidadeEstoque() == 3,
                "Deve cancelar pedido e estornar automaticamente o estoque dos produtos");
    }

    private static void afirmar(boolean condicao, String descricaoTeste) {
        testesExecutados++;
        if (condicao) {
            testesAprovados++;
            System.out.println("  [OK] " + descricaoTeste);
        } else {
            System.err.println("  [FALHA] " + descricaoTeste);
        }
    }

    private static void iniciarInterfaceDesktop(
            AutenticacaoService autenticacaoService,
            ProdutoService produtoService,
            CarrinhoService carrinhoService,
            PedidoService pedidoService,
            Cliente clientePadrao) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            TechNovaDesktopUI janela = new TechNovaDesktopUI(
                    autenticacaoService,
                    produtoService,
                    carrinhoService,
                    pedidoService,
                    clientePadrao
            );
            janela.setVisible(true);
        });
    }
}
