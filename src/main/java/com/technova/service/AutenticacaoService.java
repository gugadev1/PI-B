package com.technova.service;

import com.technova.domain.Usuario;
import com.technova.exception.RegraNegocioException;
import com.technova.exception.ValidacaoException;
import com.technova.repository.UsuarioRepository;

/**
 * Servico responsavel exclusivamente pela autenticacao e sessao de usuarios (SRP).
 * Extraido de TechNovaDesktopUI.autenticarUsuario() para reutilizacao em ambiente Web.
 */
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private Usuario usuarioAutenticado;

    public AutenticacaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new ValidacaoException("Informe email e senha para realizar o login.");
        }

        Usuario usuario = usuarioRepository.buscarPorEmail(email.trim())
                .orElseThrow(() -> new RegraNegocioException("Credenciais invalidas."));

        if (!usuario.verificarSenha(senha)) {
            throw new RegraNegocioException("Credenciais invalidas.");
        }

        this.usuarioAutenticado = usuario;
        return usuario;
    }

    public void encerrarSessao() {
        this.usuarioAutenticado = null;
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    public boolean estaAutenticado() {
        return usuarioAutenticado != null;
    }
}
