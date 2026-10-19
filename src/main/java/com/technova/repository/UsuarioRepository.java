package com.technova.repository;

import com.technova.domain.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Interface de repositorio para consulta e persistencia de Usuarios (DIP e ISP).
 */
public interface UsuarioRepository {
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorEmail(String email);
    List<Usuario> listarTodos();
}
