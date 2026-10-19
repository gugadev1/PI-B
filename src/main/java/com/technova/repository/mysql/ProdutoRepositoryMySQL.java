package com.technova.repository.mysql;

import com.technova.domain.Produto;
import com.technova.exception.PersistenciaException;
import com.technova.repository.ProdutoRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacao MySQL de ProdutoRepository (refatoracao de ProdutoDAO).
 * Responsavel exclusivamente pelo acesso a dados JDBC (SRP), sem validacoes de negocio duplicadas.
 */
public class ProdutoRepositoryMySQL implements ProdutoRepository {

    private final ConnectionFactory connectionFactory;

    public ProdutoRepositoryMySQL() {
        this(new ConnectionFactory());
    }

    public ProdutoRepositoryMySQL(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Produto salvar(Produto produto) {
        String sql = "INSERT INTO produtos (nome, descricao, categoria, preco, quantidade_estoque, ativo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherParametrosProduto(ps, produto);
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    produto.setId(chaves.getInt(1));
                }
            }
            return produto;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao cadastrar produto no banco de dados.", e);
        }
    }

    @Override
    public Produto atualizar(Produto produto) {
        String sql = "UPDATE produtos SET nome = ?, descricao = ?, categoria = ?, preco = ?, "
                + "quantidade_estoque = ?, ativo = ? WHERE id = ?";

        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            preencherParametrosProduto(ps, produto);
            ps.setInt(7, produto.getId());
            ps.executeUpdate();
            return produto;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar produto no banco de dados.", e);
        }
    }

    @Override
    public boolean remover(int id) {
        String sql = "DELETE FROM produtos WHERE id = ?";

        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao remover produto do banco de dados.", e);
        }
    }

    @Override
    public Optional<Produto> buscarPorId(int id) {
        String sql = "SELECT id, nome, descricao, categoria, preco, quantidade_estoque, ativo "
                + "FROM produtos WHERE id = ?";

        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearProduto(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar produto por ID.", e);
        }
    }

    @Override
    public List<Produto> listarTodos() {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT id, nome, descricao, categoria, preco, quantidade_estoque, ativo "
                + "FROM produtos ORDER BY nome";

        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                produtos.add(mapearProduto(rs));
            }
            return produtos;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar produtos do banco de dados.", e);
        }
    }

    @Override
    public boolean atualizarEstoque(int idProduto, int novaQuantidade) {
        String sql = "UPDATE produtos SET quantidade_estoque = ? WHERE id = ?";

        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, novaQuantidade);
            ps.setInt(2, idProduto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar estoque do produto.", e);
        }
    }

    private void preencherParametrosProduto(PreparedStatement ps, Produto produto) throws SQLException {
        ps.setString(1, produto.getNome());
        ps.setString(2, produto.getDescricao());
        ps.setString(3, produto.getCategoria());
        ps.setDouble(4, produto.getPreco());
        ps.setInt(5, produto.getQuantidadeEstoque());
        ps.setBoolean(6, produto.isAtivo());
    }

    private Produto mapearProduto(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getString("categoria"),
                rs.getDouble("preco"),
                rs.getInt("quantidade_estoque"),
                rs.getBoolean("ativo")
        );
    }
}
