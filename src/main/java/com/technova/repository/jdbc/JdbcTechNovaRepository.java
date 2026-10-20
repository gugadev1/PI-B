package com.technova.repository.jdbc;

import com.technova.domain.Cliente;
import com.technova.domain.ItemPedido;
import com.technova.domain.Pedido;
import com.technova.domain.PerfilUsuario;
import com.technova.domain.Produto;
import com.technova.domain.StatusPedido;
import com.technova.domain.Usuario;
import com.technova.exception.PersistenciaException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

@Component
public class JdbcTechNovaRepository {
    private final JdbcTemplate jdbc;

    public JdbcTechNovaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Usuario salvarUsuario(Usuario usuario) {
        long id = inserir("INSERT INTO usuarios (nome, email, senha_hash, perfil) VALUES (?, ?, ?, ?)",
                ps -> preencherUsuario(ps, usuario));
        usuario.setId(Math.toIntExact(id));
        return usuario;
    }

    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        List<Usuario> usuarios = jdbc.query(
                "SELECT id, nome, email, senha_hash, perfil FROM usuarios WHERE LOWER(email) = LOWER(?)",
                (rs, row) -> mapearUsuario(rs), email);
        return usuarios.stream().findFirst();
    }

    public List<Usuario> listarUsuarios() {
        return jdbc.query("SELECT id, nome, email, senha_hash, perfil FROM usuarios ORDER BY nome",
                (rs, row) -> mapearUsuario(rs));
    }

    public Optional<Usuario> buscarUsuarioPorId(int id) {
        return jdbc.query("SELECT id, nome, email, senha_hash, perfil FROM usuarios WHERE id = ?",
                (rs, row) -> mapearUsuario(rs), id).stream().findFirst();
    }

    public Optional<Integer> buscarIdClientePorUsuario(int usuarioId) {
        return jdbc.query("SELECT id FROM clientes WHERE usuario_id = ?", (rs, row) -> rs.getInt(1), usuarioId)
                .stream().findFirst();
    }

    public long contarUsuarios() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM usuarios", Long.class);
    }

    public long contarProdutos() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM produtos", Long.class);
    }

    public Cliente salvarCliente(Cliente cliente) {
        Usuario usuario = new Usuario(0, cliente.getNome(), cliente.getEmail(),
                cliente.getSenhaHash(), PerfilUsuario.CLIENTE);
        salvarUsuario(usuario);
        long id = inserir("INSERT INTO clientes (usuario_id, telefone) VALUES (?, ?)",
                ps -> {
                    ps.setInt(1, usuario.getId());
                    ps.setString(2, cliente.getTelefone());
                });
        cliente.setId(Math.toIntExact(id));
        return cliente;
    }

    public Cliente atualizarCliente(Cliente cliente) {
        int alterados = jdbc.update("UPDATE usuarios SET nome = ?, email = ?, senha_hash = ? "
                        + "WHERE id = (SELECT usuario_id FROM clientes WHERE id = ?)",
                cliente.getNome(), cliente.getEmail(), cliente.getSenhaHash(), cliente.getId());
        if (alterados == 0) {
            return cliente;
        }
        jdbc.update("UPDATE clientes SET telefone = ? WHERE id = ?", cliente.getTelefone(), cliente.getId());
        return cliente;
    }

    public Optional<Cliente> buscarClientePorId(int id) {
        return jdbc.query("SELECT c.id, u.nome, u.email, u.senha_hash, c.telefone "
                        + "FROM clientes c JOIN usuarios u ON u.id = c.usuario_id WHERE c.id = ?",
                (rs, row) -> mapearCliente(rs), id).stream().findFirst();
    }

    public List<Cliente> listarClientes() {
        return jdbc.query("SELECT c.id, u.nome, u.email, u.senha_hash, c.telefone "
                        + "FROM clientes c JOIN usuarios u ON u.id = c.usuario_id ORDER BY u.nome",
                (rs, row) -> mapearCliente(rs));
    }

    public Produto salvarProduto(Produto produto) {
        long id = inserir("INSERT INTO produtos "
                        + "(nome, descricao, categoria, preco, quantidade_estoque, ativo) VALUES (?, ?, ?, ?, ?, ?)",
                ps -> preencherProduto(ps, produto));
        produto.setId(Math.toIntExact(id));
        return produto;
    }

    public Produto atualizarProduto(Produto produto) {
        jdbc.update("UPDATE produtos SET nome = ?, descricao = ?, categoria = ?, preco = ?, "
                        + "quantidade_estoque = ?, ativo = ? WHERE id = ?",
                produto.getNome(), produto.getDescricao(), produto.getCategoria(), produto.getPreco(),
                produto.getQuantidadeEstoque(), produto.isAtivo(), produto.getId());
        return produto;
    }

    public boolean removerProduto(int id) {
        return jdbc.update("DELETE FROM produtos WHERE id = ?", id) > 0;
    }

    public Optional<Produto> buscarProdutoPorId(int id) {
        return jdbc.query("SELECT id, nome, descricao, categoria, preco, quantidade_estoque, ativo "
                        + "FROM produtos WHERE id = ?", (rs, row) -> mapearProduto(rs), id).stream().findFirst();
    }

    public List<Produto> listarProdutos() {
        return jdbc.query("SELECT id, nome, descricao, categoria, preco, quantidade_estoque, ativo "
                        + "FROM produtos ORDER BY nome", (rs, row) -> mapearProduto(rs));
    }

    public boolean atualizarEstoque(int idProduto, int novaQuantidade) {
        if (novaQuantidade < 0) {
            return false;
        }
        return jdbc.update("UPDATE produtos SET quantidade_estoque = ? WHERE id = ?", novaQuantidade, idProduto) > 0;
    }

    public boolean debitarEstoque(int idProduto, int quantidade) {
        return quantidade > 0 && jdbc.update("UPDATE produtos SET quantidade_estoque = quantidade_estoque - ? "
                + "WHERE id = ? AND quantidade_estoque >= ?", quantidade, idProduto, quantidade) > 0;
    }

    public boolean estornarEstoque(int idProduto, int quantidade) {
        return quantidade > 0 && jdbc.update("UPDATE produtos SET quantidade_estoque = quantidade_estoque + ? "
                + "WHERE id = ?", quantidade, idProduto) > 0;
    }

    public Pedido salvarPedido(Pedido pedido) {
        pedido.recalcularTotal();
        long id = inserir("INSERT INTO pedidos (cliente_id, status, total) VALUES (?, ?, ?)",
                ps -> {
                    ps.setInt(1, pedido.getCliente().getId());
                    ps.setString(2, pedido.getStatus().name());
                    ps.setDouble(3, pedido.getTotal());
                });
        pedido.setId(Math.toIntExact(id));
        for (ItemPedido item : pedido.getItens()) {
            jdbc.update("INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_unitario) "
                            + "VALUES (?, ?, ?, ?)",
                    pedido.getId(), item.getProduto().getId(), item.getQuantidade(), item.getPrecoUnitario());
        }
        return pedido;
    }

    public Optional<Pedido> buscarPedidoPorId(int id) {
        return listarPedidos("WHERE p.id = ?", id).stream().findFirst();
    }

    public List<Pedido> listarTodosPedidos() {
        return listarPedidos("", null);
    }

    public List<Pedido> listarPedidosPorCliente(int idCliente) {
        return listarPedidos("WHERE p.cliente_id = ?", idCliente);
    }

    public boolean atualizarStatus(int idPedido, StatusPedido novoStatus) {
        return jdbc.update("UPDATE pedidos SET status = ? WHERE id = ?", novoStatus.name(), idPedido) > 0;
    }

    public boolean atualizarStatusCondicional(int idPedido, StatusPedido statusAtual, StatusPedido novoStatus) {
        return jdbc.update("UPDATE pedidos SET status = ? WHERE id = ? AND status = ?",
                novoStatus.name(), idPedido, statusAtual.name()) > 0;
    }

    private List<Pedido> listarPedidos(String filtro, Integer parametro) {
        String sql = "SELECT p.id, p.cliente_id, p.data_criacao, p.status, p.total, "
                + "c.usuario_id, u.nome AS cliente_nome, u.email AS cliente_email, "
                + "u.senha_hash AS cliente_senha, c.telefone "
                + "FROM pedidos p JOIN clientes c ON c.id = p.cliente_id "
                + "JOIN usuarios u ON u.id = c.usuario_id " + filtro
                + " ORDER BY p.data_criacao DESC, p.id DESC";
        List<Pedido> pedidos = parametro == null
                ? jdbc.query(sql, (rs, row) -> mapearPedido(rs))
                : jdbc.query(sql, (rs, row) -> mapearPedido(rs), parametro);
        for (Pedido pedido : pedidos) {
            List<ItemPedido> itens = jdbc.query("SELECT i.produto_id, i.quantidade, i.preco_unitario, "
                            + "p.nome, p.descricao, p.categoria, p.quantidade_estoque, p.ativo "
                            + "FROM itens_pedido i JOIN produtos p ON p.id = i.produto_id WHERE i.pedido_id = ?",
                    (rs, row) -> mapearItem(rs), pedido.getId());
            itens.forEach(pedido::adicionarItem);
        }
        return pedidos;
    }

    private long inserir(String sql, org.springframework.jdbc.core.PreparedStatementSetter setter) {
        KeyHolder chave = new GeneratedKeyHolder();
        jdbc.update(conexao -> {
            PreparedStatement statement = conexao.prepareStatement(sql, new String[]{"ID"});
            setter.setValues(statement);
            return statement;
        }, chave);
        Number id = chave.getKey();
        if (id == null) {
            throw new PersistenciaException("O banco de dados nao retornou o identificador gerado.");
        }
        return id.longValue();
    }

    private void preencherUsuario(PreparedStatement ps, Usuario usuario) throws SQLException {
        ps.setString(1, usuario.getNome());
        ps.setString(2, usuario.getEmail());
        ps.setString(3, usuario.getSenhaHash());
        ps.setString(4, usuario.getPerfil().name());
    }

    private void preencherProduto(PreparedStatement ps, Produto produto) throws SQLException {
        ps.setString(1, produto.getNome());
        ps.setString(2, produto.getDescricao());
        ps.setString(3, produto.getCategoria());
        ps.setDouble(4, produto.getPreco());
        ps.setInt(5, produto.getQuantidadeEstoque());
        ps.setBoolean(6, produto.isAtivo());
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"),
                rs.getString("senha_hash"), PerfilUsuario.valueOf(rs.getString("perfil")));
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id"), rs.getString("nome"), rs.getString("email"),
                rs.getString("senha_hash"), rs.getString("telefone"));
    }

    private Produto mapearProduto(ResultSet rs) throws SQLException {
        return new Produto(rs.getInt("id"), rs.getString("nome"), rs.getString("descricao"),
                rs.getString("categoria"), rs.getDouble("preco"), rs.getInt("quantidade_estoque"),
                rs.getBoolean("ativo"));
    }

    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setId(rs.getInt("id"));
        Cliente cliente = new Cliente(rs.getInt("cliente_id"), rs.getString("cliente_nome"),
                rs.getString("cliente_email"), rs.getString("cliente_senha"), rs.getString("telefone"));
        pedido.setCliente(cliente);
        Timestamp criadoEm = rs.getTimestamp("data_criacao");
        if (criadoEm != null) {
            pedido.setDataCriacao(criadoEm.toLocalDateTime());
        }
        pedido.setStatus(StatusPedido.valueOf(rs.getString("status")));
        pedido.setTotal(rs.getDouble("total"));
        return pedido;
    }

    private ItemPedido mapearItem(ResultSet rs) throws SQLException {
        Produto produto = new Produto(rs.getInt("produto_id"), rs.getString("nome"), rs.getString("descricao"),
                rs.getString("categoria"), rs.getDouble("preco_unitario"),
                rs.getInt("quantidade_estoque"), rs.getBoolean("ativo"));
        return new ItemPedido(produto, rs.getInt("quantidade"), rs.getDouble("preco_unitario"));
    }
}
