package com.technova.web;

import static com.technova.web.ApiDtos.*;

import com.technova.domain.Cliente;
import com.technova.domain.ItemPedido;
import com.technova.domain.Pedido;
import com.technova.domain.PerfilUsuario;
import com.technova.domain.Produto;
import com.technova.domain.StatusPedido;
import com.technova.domain.Usuario;
import com.technova.exception.RegraNegocioException;
import com.technova.exception.RecursoNaoEncontradoException;
import com.technova.exception.ValidacaoException;
import com.technova.repository.PedidoRepository;
import com.technova.repository.jdbc.JdbcTechNovaRepository;
import com.technova.security.SenhaHasher;
import com.technova.service.AutenticacaoService;
import com.technova.service.CarrinhoService;
import com.technova.service.ClienteService;
import com.technova.service.PedidoService;
import com.technova.service.ProdutoService;
import java.math.BigDecimal;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class TechNovaApiController {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final AutenticacaoService autenticacaoService;
    private final ProdutoService produtoService;
    private final ClienteService clienteService;
    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;
    private final JdbcTechNovaRepository repository;

    public TechNovaApiController(AutenticacaoService autenticacaoService,
            ProdutoService produtoService, ClienteService clienteService,
            PedidoService pedidoService, PedidoRepository pedidoRepository,
            JdbcTechNovaRepository repository) {
        this.autenticacaoService = autenticacaoService;
        this.produtoService = produtoService;
        this.clienteService = clienteService;
        this.pedidoService = pedidoService;
        this.pedidoRepository = pedidoRepository;
        this.repository = repository;
    }

    @GetMapping("/session")
    public SessionOutput session(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new SessionOutput(null);
        }
        Object id = session.getAttribute("usuarioId");
        if (!(id instanceof Integer usuarioId)) {
            return new SessionOutput(null);
        }
        return new SessionOutput(repository.buscarUsuarioPorId(usuarioId)
                .map(this::usuarioOutput).orElse(null));
    }

    @PostMapping("/auth/login")
    public SessionOutput login(@RequestBody LoginInput input, HttpServletRequest request) {
        if (input == null) {
            throw new ValidacaoException("Informe e-mail e senha.");
        }
        Usuario usuario;
        try {
            usuario = autenticacaoService.autenticar(input.email(), input.password());
        } catch (ValidacaoException e) {
            throw e;
        } catch (RegraNegocioException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
        }
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute("usuarioId", usuario.getId());
        return new SessionOutput(usuarioOutput(usuario));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products")
    public List<ProdutoOutput> products(HttpServletRequest request) {
        Usuario usuario = usuarioAtual(request);
        return produtoService.listarProdutos().stream()
                .filter(produto -> usuario.getPerfil() != PerfilUsuario.CLIENTE || produto.isAtivo())
                .map(this::produtoOutput).toList();
    }

    @PostMapping("/products")
    @Transactional
    public ResponseEntity<ProdutoOutput> createProduct(@RequestBody ProdutoInput input, HttpServletRequest request) {
        exigirEquipe(request);
        validarProduto(input);
        Produto produto = new Produto(0, input.name().trim(), textoOpcional(input.description()),
                input.category().trim(), input.price(), input.stock(), true);
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoOutput(produtoService.cadastrarProduto(produto)));
    }

    @PutMapping("/products/{id}")
    @Transactional
    public ProdutoOutput updateProduct(@PathVariable int id, @RequestBody ProdutoInput input,
            HttpServletRequest request) {
        exigirEquipe(request);
        validarProduto(input);
        Produto existente = produtoService.buscarPorId(id);
        existente.setNome(input.name().trim());
        existente.setDescricao(textoOpcional(input.description()));
        existente.setCategoria(input.category().trim());
        existente.setPreco(input.price());
        existente.setQuantidadeEstoque(input.stock());
        return produtoOutput(produtoService.atualizarProduto(existente));
    }

    @PatchMapping("/products/{id}/active")
    @Transactional
    public ProdutoOutput toggleProduct(@PathVariable int id, HttpServletRequest request) {
        exigirAdministrador(request);
        Produto produto = produtoService.buscarPorId(id);
        produto.setAtivo(!produto.isAtivo());
        return produtoOutput(produtoService.atualizarProduto(produto));
    }

    @GetMapping("/clients")
    public List<ClienteOutput> clients(HttpServletRequest request) {
        exigirEquipe(request);
        return clienteService.listarClientes().stream().map(this::clienteOutput).toList();
    }

    @PostMapping("/clients")
    @Transactional
    public ResponseEntity<ClienteOutput> createClient(@RequestBody ClienteInput input,
            HttpServletRequest request) {
        exigirEquipe(request);
        validarCliente(input);
        Cliente cliente = new Cliente(0, input.name().trim(), input.email().trim().toLowerCase(Locale.ROOT),
                SenhaHasher.hash(input.password()), input.phone().trim());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteOutput(clienteService.cadastrarCliente(cliente)));
    }

    @GetMapping("/orders")
    public List<PedidoOutput> orders(HttpServletRequest request) {
        Usuario usuario = usuarioAtual(request);
        Integer clienteId = usuario.getPerfil() == PerfilUsuario.CLIENTE
                ? repository.buscarIdClientePorUsuario(usuario.getId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro do cliente não encontrado."))
                : null;
        List<Pedido> pedidos = clienteId == null
                ? pedidoService.listarTodos() : pedidoService.listarPorCliente(clienteId);
        return pedidos.stream().map(this::pedidoOutput).toList();
    }

    @PostMapping("/orders")
    @Transactional
    public ResponseEntity<PedidoOutput> createOrder(@RequestBody PedidoInput input,
            HttpServletRequest request) {
        Usuario usuario = usuarioAtual(request);
        if (input == null || input.items() == null || input.items().isEmpty()) {
            throw new ValidacaoException("O pedido deve conter ao menos um item.");
        }
        Integer clienteId = input.clientId();
        if (usuario.getPerfil() == PerfilUsuario.CLIENTE) {
            int clienteAutenticado = repository.buscarIdClientePorUsuario(usuario.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro do cliente não encontrado."));
            if (clienteId != null && clienteId != clienteAutenticado) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Clientes só podem criar pedidos para o próprio cadastro.");
            }
            clienteId = clienteAutenticado;
        }
        if (clienteId == null || clienteId <= 0) {
            throw new ValidacaoException("Selecione um cliente válido para o pedido.");
        }

        Cliente cliente = clienteService.buscarPorId(clienteId);
        CarrinhoService carrinho = new CarrinhoService();
        for (ItemPedidoInput item : input.items()) {
            if (item == null || item.productId() == null || item.quantity() == null
                    || item.productId() <= 0 || item.quantity() <= 0) {
                throw new ValidacaoException("Produto e quantidade válidos são obrigatórios para cada item.");
            }
            carrinho.adicionarItem(produtoService.buscarPorId(item.productId()), item.quantity());
        }
        Pedido pedido = pedidoService.criarPedido(cliente, carrinho.listarItens());
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoOutput(pedido));
    }

    @PutMapping("/orders/{id}/status")
    @Transactional
    public PedidoOutput updateOrderStatus(@PathVariable int id, @RequestBody StatusInput input,
            HttpServletRequest request) {
        Usuario usuario = exigirAdministrador(request);
        if (input == null || input.status() == null || input.status().isBlank()) {
            throw new ValidacaoException("Informe o novo status do pedido.");
        }
        StatusPedido status;
        try {
            status = StatusPedido.valueOf(input.status());
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("Status de pedido inválido.");
        }
        Pedido pedido = pedidoRepository.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        pedidoService.atualizarStatusPedido(pedido, status, usuario);
        return pedidoRepository.buscarPorId(id).map(this::pedidoOutput)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
    }

    private Usuario usuarioAtual(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute("usuarioId");
        if (!(id instanceof Integer usuarioId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação necessária.");
        }
        return repository.buscarUsuarioPorId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida."));
    }

    private Usuario exigirEquipe(HttpServletRequest request) {
        Usuario usuario = usuarioAtual(request);
        if (usuario.getPerfil() == PerfilUsuario.CLIENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso permitido apenas à equipe.");
        }
        return usuario;
    }

    private Usuario exigirAdministrador(HttpServletRequest request) {
        Usuario usuario = usuarioAtual(request);
        if (usuario.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas administradores podem realizar esta operação.");
        }
        return usuario;
    }

    private void validarProduto(ProdutoInput input) {
        if (input == null || input.name() == null || input.name().isBlank()
                || input.category() == null || input.category().isBlank()
                || input.price() == null || input.stock() == null) {
            throw new ValidacaoException("Preencha nome, categoria, preço e estoque.");
        }
        if (input.name().trim().length() > 80 || input.category().trim().length() > 50
                || (input.description() != null && input.description().length() > 180)
                || !Double.isFinite(input.price()) || input.price() < 0
                || input.price() > 9_999_999_999.99
                || BigDecimal.valueOf(input.price()).scale() > 2
                || input.stock() < 0) {
            throw new ValidacaoException("Os dados do produto estão fora dos limites permitidos.");
        }
    }

    private void validarCliente(ClienteInput input) {
        if (input == null || input.name() == null || input.name().isBlank()
                || input.email() == null || input.email().isBlank()
                || input.phone() == null || input.phone().isBlank()
                || input.password() == null || input.password().length() < 8
                || input.password().length() > 100) {
            throw new ValidacaoException("Informe nome, e-mail, telefone e senha (8 a 100 caracteres).");
        }
        if (input.name().trim().length() > 100 || input.phone().trim().length() > 20
                || input.email().trim().length() > 120 || !EMAIL.matcher(input.email().trim()).matches()) {
            throw new ValidacaoException("Informe dados válidos para o cadastro do cliente.");
        }
    }

    private String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private UsuarioOutput usuarioOutput(Usuario usuario) {
        Integer clienteId = usuario.getPerfil() == PerfilUsuario.CLIENTE
                ? repository.buscarIdClientePorUsuario(usuario.getId()).orElse(null) : null;
        return new UsuarioOutput(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil().name(), clienteId);
    }

    private ProdutoOutput produtoOutput(Produto produto) {
        return new ProdutoOutput(produto.getId(), produto.getNome(), produto.getDescricao(),
                produto.getCategoria(), produto.getPreco(), produto.getQuantidadeEstoque(), produto.isAtivo());
    }

    private ClienteOutput clienteOutput(Cliente cliente) {
        return new ClienteOutput(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.getTelefone());
    }

    private PedidoOutput pedidoOutput(Pedido pedido) {
        List<ItemPedidoOutput> itens = pedido.getItens().stream()
                .map(item -> new ItemPedidoOutput(item.getProduto().getId(), item.getProduto().getNome(),
                        item.getQuantidade(), item.getPrecoUnitario(), item.getSubtotal())).toList();
        return new PedidoOutput(pedido.getId(), pedido.getCliente().getId(), pedido.getCliente().getNome(),
                pedido.getDataCriacao(), pedido.getStatus().name(), itens, pedido.getTotal());
    }
}
