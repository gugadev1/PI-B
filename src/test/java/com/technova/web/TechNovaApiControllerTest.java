package com.technova.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technova.domain.Cliente;
import com.technova.domain.PerfilUsuario;
import com.technova.domain.Produto;
import com.technova.domain.Usuario;
import com.technova.app.TechNovaWebApplication;
import com.technova.repository.jdbc.JdbcTechNovaRepository;
import com.technova.security.SenhaHasher;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest(classes = TechNovaWebApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:technova-api-tests;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class TechNovaApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JdbcTechNovaRepository repository;

    private int clienteId;
    private int outroClienteId;
    private int produtoId;

    @BeforeEach
    void prepararDados() {
        jdbc.update("DELETE FROM itens_pedido");
        jdbc.update("DELETE FROM pedidos");
        jdbc.update("DELETE FROM clientes");
        jdbc.update("DELETE FROM usuarios");
        jdbc.update("DELETE FROM produtos");

        repository.salvarUsuario(new Usuario(0, "Administrador", "admin@technova.com",
                SenhaHasher.hash("admin123"), PerfilUsuario.ADMINISTRADOR));
        repository.salvarUsuario(new Usuario(0, "Funcionária", "vendas@technova.com",
                SenhaHasher.hash("vendas123"), PerfilUsuario.FUNCIONARIO));
        clienteId = repository.salvarCliente(new Cliente(0, "Cliente Um", "um@example.com",
                SenhaHasher.hash("cliente123"), "(51) 99999-0001")).getId();
        outroClienteId = repository.salvarCliente(new Cliente(0, "Cliente Dois", "dois@example.com",
                SenhaHasher.hash("cliente123"), "(51) 99999-0002")).getId();
        produtoId = repository.salvarProduto(new Produto(0, "Teclado", "Mecânico",
                "Periféricos", 150.50, 5, true)).getId();
    }

    @Test
    void loginValidaCredenciaisEExigeSessaoParaRecursosProtegidos() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "admin@technova.com", "password", "errada"))))
                .andExpect(status().isUnauthorized());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "admin@technova.com", "password", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMINISTRADOR"))
                .andReturn();

        mockMvc.perform(get("/api/products").session(sessao(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Teclado"));

        mockMvc.perform(post("/api/auth/logout").session(sessao(login)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permiteGerenciarCatalogoPorPerfilERejeitaDadosInvalidos() throws Exception {
        MockHttpSession funcionario = login("vendas@technova.com", "vendas123");
        MvcResult criado = mockMvc.perform(post("/api/products").session(funcionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Mouse", "category", "Periféricos",
                                "price", 70, "stock", 3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();
        int mouseId = objectMapper.readTree(criado.getResponse().getContentAsString()).get("id").asInt();
        mockMvc.perform(put("/api/products/" + mouseId).session(funcionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Mouse sem fio", "description", "Bluetooth",
                                "category", "Periféricos", "price", 75.50, "stock", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mouse sem fio"))
                .andExpect(jsonPath("$.stock").value(4));

        mockMvc.perform(put("/api/products/999999").session(funcionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Mouse", "category", "Periféricos",
                                "price", 70, "stock", 3))))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/products/" + produtoId + "/active").session(funcionario))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/products").session(funcionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Invalido", "category", "X",
                                "price", -1, "stock", 2))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        MockHttpSession cliente = login("um@example.com", "cliente123");
        mockMvc.perform(get("/api/clients").session(cliente))
                .andExpect(status().isForbidden());
    }

    @Test
    void cadastraClienteComSenhaHasheadaEValidaEmail() throws Exception {
        MockHttpSession funcionario = login("vendas@technova.com", "vendas123");
        String novoCliente = json(Map.of("name", "Ana Costa", "email", "ana@example.com",
                "password", "senha1234", "phone", "(51) 99999-0010"));
        MvcResult criado = mockMvc.perform(post("/api/clients").session(funcionario)
                        .contentType(MediaType.APPLICATION_JSON).content(novoCliente))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andReturn();

        String senhaHash = jdbc.queryForObject(
                "SELECT u.senha_hash FROM usuarios u JOIN clientes c ON c.usuario_id = u.id WHERE c.id = ?",
                String.class, objectMapper.readTree(criado.getResponse().getContentAsString()).get("id").asInt());
        org.junit.jupiter.api.Assertions.assertNotEquals("senha1234", senhaHash);
        org.junit.jupiter.api.Assertions.assertTrue(senhaHash.startsWith("pbkdf2$"));
        login("ana@example.com", "senha1234");

        mockMvc.perform(post("/api/clients").session(funcionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Inválida", "email", "email-invalido",
                                "password", "senha1234", "phone", "123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criaPedidoComEstoqueTransacionalEFiltraPedidosDoCliente() throws Exception {
        MockHttpSession cliente = login("um@example.com", "cliente123");
        String pedido = json(Map.of("items", new Object[]{Map.of("productId", produtoId, "quantity", 2)}));
        MvcResult criado = mockMvc.perform(post("/api/orders").session(cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(pedido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EM_PROCESSAMENTO"))
                .andExpect(jsonPath("$.total").value(301.0))
                .andReturn();
        int idPedido = objectMapper.readTree(criado.getResponse().getContentAsString()).get("id").asInt();
        assertEstoque(3);

        mockMvc.perform(get("/api/orders").session(cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(idPedido));
        mockMvc.perform(get("/api/orders").session(login("dois@example.com", "cliente123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/orders").session(cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("clientId", outroClienteId,
                                "items", new Object[]{Map.of("productId", produtoId, "quantity", 1)}))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/orders/" + idPedido + "/status")
                        .session(login("vendas@technova.com", "vendas123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "APROVADO"))))
                .andExpect(status().isForbidden());

        MockHttpSession admin = login("admin@technova.com", "admin123");
        mockMvc.perform(put("/api/orders/" + idPedido + "/status").session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "APROVADO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        mockMvc.perform(put("/api/orders/" + idPedido + "/status").session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "CANCELADO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
        assertEstoque(5);

        mockMvc.perform(put("/api/orders/" + idPedido + "/status").session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "APROVADO"))))
                .andExpect(status().isConflict());
        assertEstoque(5);

        mockMvc.perform(post("/api/orders").session(cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("items", new Object[]{Map.of("productId", produtoId, "quantity", 6)}))))
                .andExpect(status().isConflict());
        assertEstoque(5);
    }

    private MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return sessao(result);
    }

    private MockHttpSession sessao(MvcResult result) {
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private void assertEstoque(int esperado) {
        int atual = jdbc.queryForObject("SELECT quantidade_estoque FROM produtos WHERE id = ?",
                Integer.class, produtoId);
        org.junit.jupiter.api.Assertions.assertEquals(esperado, atual);
    }
}
