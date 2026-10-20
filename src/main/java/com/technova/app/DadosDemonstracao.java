package com.technova.app;

import com.technova.domain.Cliente;
import com.technova.domain.PerfilUsuario;
import com.technova.domain.Produto;
import com.technova.domain.Usuario;
import com.technova.repository.jdbc.JdbcTechNovaRepository;
import com.technova.security.SenhaHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DadosDemonstracao implements CommandLineRunner {
    private final JdbcTechNovaRepository repository;

    public DadosDemonstracao(JdbcTechNovaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.contarUsuarios() == 0) {
            repository.salvarUsuario(new Usuario(0, "Administrador TechNova", "admin@technova.com",
                    SenhaHasher.hash("admin123"), PerfilUsuario.ADMINISTRADOR));
            repository.salvarUsuario(new Usuario(0, "Marina Oliveira", "vendas@technova.com",
                    SenhaHasher.hash("vendas123"), PerfilUsuario.FUNCIONARIO));
            Cliente cliente = new Cliente(0, "Cliente Balcão", "cliente.balcao@technova.com",
                    SenhaHasher.hash("123456"), "(51) 99999-0000");
            repository.salvarCliente(cliente);
            repository.salvarCliente(new Cliente(0, "Bruno Lima", "bruno@email.com",
                    SenhaHasher.hash("bruno123"), "(51) 99999-0002"));
        }

        if (repository.contarProdutos() == 0) {
            repository.salvarProduto(new Produto(0, "Notebook Pro 14",
                    "Desempenho profissional em um design compacto.", "Notebooks", 6499.90, 8, true));
            repository.salvarProduto(new Produto(0, "Mouse sem fio",
                    "Conexão estável e bateria de longa duração.", "Periféricos", 129.90, 40, true));
            repository.salvarProduto(new Produto(0, "Monitor 27” 4K",
                    "Imagem nítida para trabalho e entretenimento.", "Monitores", 2199.00, 5, true));
            repository.salvarProduto(new Produto(0, "Webcam HD",
                    "Imagem em alta definição para reuniões.", "Periféricos", 189.00, 12, false));
        }
    }
}
