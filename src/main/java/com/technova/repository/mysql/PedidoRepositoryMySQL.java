package com.technova.repository.mysql;

import com.technova.domain.Cliente;
import com.technova.domain.ItemPedido;
import com.technova.domain.Pedido;
import com.technova.domain.Produto;
import com.technova.domain.StatusPedido;
import com.technova.exception.PersistenciaException;
import com.technova.repository.PedidoRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementacao MySQL de PedidoRepository (refatoracao de PedidoDAO).
 * Aplica Extract Method para decompor os metodos longos de transacao e mapeamento SQL.
 */
public class PedidoRepositoryMySQL implements PedidoRepository {

    private final ConnectionFactory connectionFactory;

    public PedidoRepositoryMySQL() {
        this(new ConnectionFactory());
    }

    public PedidoRepositoryMySQL(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        pedido.recalcularTotal();

        try (Connection conexao = connectionFactory.obterConexao()) {
            conexao.setAutoCommit(false);
            try {
                int idPedido = inserirCabecalhoPedido(conexao, pedido);
                pedido.setId(idPedido);
                inserirItensEBaixarEstoque(conexao, idPedido, pedido.getItens());
                conexao.commit();
                return pedido;
            } catch (SQLException e) {
                conexao.rollback();
                throw new PersistenciaException("Falha na transacao ao registrar o pedido.", e);
            } finally {
                conexao.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro de conexao ao salvar pedido.", e);
        }
    }

    @Override
    public Optional<Pedido> buscarPorId(int id) {
        return listarTodos().stream().filter(p -> p.getId() == id).findFirst();
    }

    @Override
    public List<Pedido> listarTodos() {
        String sql = construirQueryBasePedidos("") + " ORDER BY p.data_criacao DESC, p.id DESC";
        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return extrairPedidosDoResultSet(rs);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar pedidos.", e);
        }
    }

    @Override
    public List<Pedido> listarPorCliente(int idCliente) {
        String sql = construirQueryBasePedidos("WHERE p.cliente_id = ?") + " ORDER BY p.data_criacao DESC, p.id DESC";
        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return extrairPedidosDoResultSet(rs);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar pedidos do cliente.", e);
        }
    }

    @Override
    public boolean atualizarStatus(int idPedido, StatusPedido novoStatus) {
        String sql = "UPDATE pedidos SET status = ? WHERE id = ?";
        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, novoStatus.name());
            ps.setInt(2, idPedido);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar status do pedido.", e);
        }
    }

    private int inserirCabecalhoPedido(Connection conexao, Pedido pedido) throws SQLException {
        String sqlPedido = "INSERT INTO pedidos (cliente_id, usuario_responsavel_id, status, total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement psPedido = conexao.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
            psPedido.setInt(1, pedido.getCliente().getId());
            psPedido.setNull(2, java.sql.Types.INTEGER);
            psPedido.setString(3, pedido.getStatus().name());
            psPedido.setDouble(4, pedido.getTotal());

            if (psPedido.executeUpdate() == 0) {
                throw new SQLException("Nenhuma linha inserida para o pedido.");
            }

            try (ResultSet chaves = psPedido.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new SQLException("ID do pedido nao foi gerado.");
                }
                return chaves.getInt(1);
            }
        }
    }

    private void inserirItensEBaixarEstoque(Connection conexao, int idPedido, List<ItemPedido> itens) throws SQLException {
        String sqlItem = "INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlBaixaEstoque = "UPDATE produtos SET quantidade_estoque = quantidade_estoque - ? "
                + "WHERE id = ? AND quantidade_estoque >= ?";

        try (PreparedStatement psItem = conexao.prepareStatement(sqlItem);
             PreparedStatement psBaixaEstoque = conexao.prepareStatement(sqlBaixaEstoque)) {

            for (ItemPedido item : itens) {
                psItem.setInt(1, idPedido);
                psItem.setInt(2, item.getProduto().getId());
                psItem.setInt(3, item.getQuantidade());
                psItem.setDouble(4, item.getPrecoUnitario());
                psItem.setDouble(5, item.getSubtotal());
                psItem.addBatch();

                psBaixaEstoque.setInt(1, item.getQuantidade());
                psBaixaEstoque.setInt(2, item.getProduto().getId());
                psBaixaEstoque.setInt(3, item.getQuantidade());
                psBaixaEstoque.addBatch();
            }

            psItem.executeBatch();
            int[] resultadosBaixa = psBaixaEstoque.executeBatch();
            for (int resultado : resultadosBaixa) {
                if (resultado == 0 || resultado == Statement.EXECUTE_FAILED) {
                    throw new SQLException("Falha ao baixar estoque de um ou mais produtos.");
                }
            }
        }
    }

    private String construirQueryBasePedidos(String clausulaWhere) {
        return "SELECT p.id AS pedido_id, p.cliente_id, p.data_criacao, p.status, p.total, "
                + "i.produto_id, i.quantidade, i.preco_unitario, "
                + "pr.nome AS produto_nome, pr.descricao, pr.categoria, pr.ativo "
                + "FROM pedidos p "
                + "LEFT JOIN itens_pedido i ON i.pedido_id = p.id "
                + "LEFT JOIN produtos pr ON pr.id = i.produto_id "
                + clausulaWhere;
    }

    private List<Pedido> extrairPedidosDoResultSet(ResultSet rs) throws SQLException {
        Map<Integer, Pedido> mapaPedidos = new LinkedHashMap<>();

        while (rs.next()) {
            int idPedido = rs.getInt("pedido_id");
            Pedido pedido = mapaPedidos.computeIfAbsent(idPedido, id -> criarCabecalhoPedido(rs, id));

            int idProduto = rs.getInt("produto_id");
            if (!rs.wasNull()) {
                pedido.adicionarItem(mapearItemPedido(rs, idProduto));
            }
        }
        return new ArrayList<>(mapaPedidos.values());
    }

    private Pedido criarCabecalhoPedido(ResultSet rs, int idPedido) {
        try {
            Pedido pedido = new Pedido();
            pedido.setId(idPedido);

            Cliente cliente = new Cliente();
            cliente.setId(rs.getInt("cliente_id"));
            pedido.setCliente(cliente);

            Timestamp timestamp = rs.getTimestamp("data_criacao");
            if (timestamp != null) {
                pedido.setDataCriacao(timestamp.toLocalDateTime());
            }

            String statusBanco = rs.getString("status");
            try {
                pedido.setStatus(StatusPedido.valueOf(statusBanco));
            } catch (IllegalArgumentException e) {
                pedido.setStatus(StatusPedido.EM_PROCESSAMENTO);
            }

            pedido.setTotal(rs.getDouble("total"));
            return pedido;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao mapear cabecalho do pedido.", e);
        }
    }

    private ItemPedido mapearItemPedido(ResultSet rs, int idProduto) throws SQLException {
        Produto produto = new Produto();
        produto.setId(idProduto);
        produto.setNome(rs.getString("produto_nome"));
        produto.setDescricao(rs.getString("descricao"));
        produto.setCategoria(rs.getString("categoria"));
        produto.setAtivo(rs.getBoolean("ativo"));

        return new ItemPedido(
                produto,
                rs.getInt("quantidade"),
                rs.getDouble("preco_unitario")
        );
    }
}
