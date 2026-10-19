package com.technova.repository.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica de conexoes JDBC (Padrao Factory), substituindo a antiga classe ConexaoMySQL.
 * Elimina o code smell de credenciais fixas (Hardcoded Credentials) ao suportar variaveis de ambiente.
 */
public class ConnectionFactory {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/technova_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private final String url;
    private final String usuario;
    private final String senha;

    public ConnectionFactory() {
        this(
            lerConfiguracao("TECHNOVA_DB_URL", DEFAULT_URL),
            lerConfiguracao("TECHNOVA_DB_USER", DEFAULT_USER),
            lerConfiguracao("TECHNOVA_DB_PASSWORD", DEFAULT_PASSWORD)
        );
    }

    public ConnectionFactory(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    public Connection obterConexao() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }

    private static String lerConfiguracao(String chave, String valorPadrao) {
        String valor = System.getenv(chave);
        return (valor == null || valor.isBlank()) ? valorPadrao : valor;
    }
}
