package com.technova.repository.mysql;

import com.technova.domain.Cliente;
import com.technova.exception.PersistenciaException;
import com.technova.repository.ClienteRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacao MySQL de ClienteRepository (refatoracao de ClienteDAO).
 * Elimina a duplicacao de mapeamento do ResultSet atraves do metodo auxiliar mapearCliente().
 */
public class ClienteRepositoryMySQL implements ClienteRepository {

    private final ConnectionFactory connectionFactory;

    public ClienteRepositoryMySQL() {
        this(new ConnectionFactory());
    }

    public ClienteRepositoryMySQL(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        String sql = "INSERT INTO clientes (nome, email, senha_hash, telefone, ativo) VALUES (?, ?, ?, ?, 1)";

        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getSenhaHash());
            ps.setString(4, cliente.getTelefone());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    cliente.setId(chaves.getInt(1));
                }
            }
            return cliente;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao salvar cliente no banco de dados.", e);
        }
    }

    @Override
    public Cliente atualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET nome = ?, email = ?, senha_hash = ?, telefone = ? WHERE id = ?";

        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getSenhaHash());
            ps.setString(4, cliente.getTelefone());
            ps.setInt(5, cliente.getId());
            ps.executeUpdate();
            return cliente;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar cliente no banco de dados.", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        String sql = "SELECT id, nome, email, senha_hash, telefone FROM clientes WHERE id = ? AND ativo = 1";

        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearCliente(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar cliente por ID.", e);
        }
    }

    @Override
    public List<Cliente> listarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, nome, email, senha_hash, telefone FROM clientes WHERE ativo = 1 ORDER BY nome";

        try (Connection conexao = connectionFactory.obterConexao();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
            return clientes;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar clientes do banco de dados.", e);
        }
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("senha_hash"),
                rs.getString("telefone")
        );
    }
}
