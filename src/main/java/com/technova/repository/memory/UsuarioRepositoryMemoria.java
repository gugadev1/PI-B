package com.technova.repository.memory;

import com.technova.domain.Usuario;
import com.technova.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacao em memoria de UsuarioRepository.
 * Substitui a lista de usuarios que ficava acoplada dentro da tela TechNovaDesktopUI.
 */
public class UsuarioRepositoryMemoria implements UsuarioRepository {
    private final List<Usuario> usuarios = new ArrayList<>();
    private int proximoId = 1;

    @Override
    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() <= 0) {
            usuario.setId(proximoId++);
        } else if (usuario.getId() >= proximoId) {
            proximoId = usuario.getId() + 1;
        }
        usuarios.add(usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return usuarios.stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    @Override
    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios);
    }
}
