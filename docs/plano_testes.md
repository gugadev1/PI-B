# Plano de Testes — TechNova

## Objetivo e escopo

Verificar as regras do núcleo, a API Spring MVC, o controle de acesso por perfil, a integração JDBC com banco relacional e os fluxos principais da interface web. A suíte automatizada usa H2 em memória para não depender de serviços externos; a aplicação local usa H2 em arquivo.

## Estratégia e dados

- **Unitários:** JUnit 5 para regras do carrinho e serviços independentes de banco.
- **Integração:** Spring Boot Test/MockMvc e H2 para requisições HTTP, sessão, persistência, permissões, estoque e transações.
- **Aceitação web:** executar a aplicação, abrir a página servida pelo Spring MVC e verificar os fluxos de login, catálogo, carrinho, pedido e cancelamento.
- Os testes de integração recriam os registros de usuários, clientes e produtos em cada caso. O acesso MySQL preexistente não é exercitado pela suíte automatizada; o perfil funcional integrado e documentado nesta etapa é H2.

## Casos

| ID | Requisito | Cenário e resultado esperado | Cobertura |
| --- | --- | --- | --- |
| CT01 | RF001 — Autenticação | Credenciais válidas iniciam sessão com perfil; senha inválida retorna 401; rotas protegidas sem sessão retornam 401. | `TechNovaApiControllerTest.loginValidaCredenciaisEExigeSessaoParaRecursosProtegidos` |
| CT02 | RF002 — Produtos | Equipe cadastra e edita produto; dados inválidos retornam 400; ID inexistente retorna 404; cliente não acessa manutenção; funcionário não ativa/inativa produto. | `TechNovaApiControllerTest.permiteGerenciarCatalogoPorPerfilERejeitaDadosInvalidos` |
| CT03 | RF003 — Clientes | Equipe cadastra cliente com senha hash e e-mail válido; o cliente consegue autenticar; e-mail inválido retorna 400. | `TechNovaApiControllerTest.cadastraClienteComSenhaHasheadaEValidaEmail` |
| CT04 | RF004 — Carrinho e totalização | Repetir produto consolida quantidade e total; exceder estoque rejeita a operação sem alterar o carrinho. | `CarrinhoServiceTest` |
| CT05 | RF005 — Pedido e estoque | Pedido válido persiste com status inicial, total correto e baixa de estoque; tentativa acima do estoque falha sem mudar o estoque. | `TechNovaApiControllerTest.criaPedidoComEstoqueTransacionalEFiltraPedidosDoCliente` |
| CT06 | RF006 — Permissões e transições | Cliente não cria pedido em nome de terceiro; funcionário não altera status; administrador aprova pedido; transição a partir de pedido cancelado é rejeitada. | `TechNovaApiControllerTest.criaPedidoComEstoqueTransacionalEFiltraPedidosDoCliente` |
| CT07 | RF007 — Cancelamento e estorno | Administrador cancela pedido elegível, recebe status `CANCELADO` e estoque retorna ao valor inicial. | `TechNovaApiControllerTest.criaPedidoComEstoqueTransacionalEFiltraPedidosDoCliente` |
| CT08 | Persistência relacional | H2 grava e consulta usuários, produtos, clientes, pedidos e itens; baixa e estorno são confirmados por nova consulta JDBC. | Testes Spring MVC/JDBC em `TechNovaApiControllerTest` |
| CT09 | Sessão e navegação | Página e JavaScript são servidos pelo Spring; o login mantém sessão; cliente vê apenas o próprio histórico; logout encerra a sessão. | Teste de navegador e `TechNovaApiControllerTest` |

## Execução

Na raiz do projeto:

```powershell
mvn clean verify
```

Para iniciar o sistema web:

```powershell
mvn spring-boot:run
```

Consulte `docs/evidencias_testes.md` para comandos realmente executados, saídas e o registro de bugtracking associado a uma falha reproduzível.
