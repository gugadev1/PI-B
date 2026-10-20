package com.technova.repository.jdbc;

import com.technova.domain.Usuario;
import com.technova.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUsuarioRepository implements UsuarioRepository {
    private final JdbcTechNovaRepository database;

    public JdbcUsuarioRepository(JdbcTechNovaRepository database) {
        this.database = database;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return database.salvarUsuario(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return database.buscarUsuarioPorEmail(email);
    }

    @Override
    public List<Usuario> listarTodos() {
        return database.listarUsuarios();
    }
}
