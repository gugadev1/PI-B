# TechNova Soluções - Projeto Refatorado (SOLID) para Reutilização Web

## Status do Projeto
Concluído (Fase de Refatoração e Preparação para Arquitetura Web - PI2).

## Tecnologias e Padrões Aplicados
- **Java 17** (Projeto Maven / Apache NetBeans)
- **HTML, CSS e JavaScript** no protótipo web estático em `src/main/resources/static`
- **Princípios SOLID** (SRP, OCP, LSP, ISP, DIP)
- **Padrões de Projeto**: Repository/DAO, Dependency Injection, Strategy (Validação), Factory (`ConnectionFactory`)
- **MySQL / JDBC** (desacoplado via interfaces de repositório)
- **Java Swing** (camada visual desacoplada das regras de negócio)

## Autor
- Gusthavo Gassen

## Estrutura de Pacotes
- `com.technova.domain`: Entidades de negócio (`Usuario`, `Cliente`, `Produto`, `Pedido`, `ItemPedido`) e enums (`PerfilUsuario`, `StatusPedido`) enriquecidos com regras de domínio.
- `com.technova.validation`: Validadores dedicados (`Validador<T>`, `ProdutoValidator`, `ClienteValidator`, `PedidoValidator`).
- `com.technova.repository`: Interfaces segregadas (`ProdutoRepository`, `ClienteRepository`, `PedidoRepository`, `UsuarioRepository`).
- `com.technova.repository.memory`: Implementações em memória para execução de testes independentes de banco de dados.
- `com.technova.repository.mysql`: Implementações JDBC/MySQL refatoradas com `ConnectionFactory`.
- `com.technova.service`: Camada de serviços de negócio reutilizável no sistema Web (`AutenticacaoService`, `ProdutoService`, `ClienteService`, `CarrinhoService`, `PedidoService`).
- `com.technova.exception`: Exceções de domínio e persistência (`RegraNegocioException`, `ValidacaoException`, `PersistenciaException`).
- `com.technova.ui`: Interface gráfica `TechNovaDesktopUI` enxuta e desacoplada.
- `com.technova.app`: Classe `TechNovaApp` com suíte de testes automatizados no método `main()`.
- `src/main/resources/static`: protótipo web demonstrativo, sem dependências de bibliotecas ou conexão com back-end.

## Protótipo web

Abra `src/main/resources/static/index.html` diretamente no navegador. O protótipo contém login por perfil, catálogo e manutenção de produtos, cadastro de clientes, carrinho e acompanhamento de pedidos. Os dados são fictícios e mantidos apenas na memória da página; atualizar ou fechar a página reinicia a demonstração. Não há servidor web, persistência, autenticação real ou conexão com o banco de dados.

Contas demonstrativas:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| Administrador | `admin@technova.com` | `admin123` |
| Funcionário | `vendas@technova.com` | `vendas123` |
| Cliente | `cliente.balcao@technova.com` | `123456` |

O administrador pode ativar/inativar produtos, atualizar o status e cancelar pedidos em processamento ou aprovados. Administradores e funcionários podem cadastrar/editar produtos e clientes e criar pedidos para um cliente selecionado. Clientes veem apenas o próprio histórico e não podem alterar pedidos. A confirmação reduz o estoque; um cancelamento autorizado devolve as quantidades.

## Testes
- Execute os testes unitários JUnit 5 com `mvn test`.
- Consulte o [plano de testes](docs/plano_testes.md) para os cenários do núcleo de negócio, da persistência e dos fluxos web previstos.
- A classe `com.technova.app.TechNovaApp` também contém verificações manuais executáveis via `mvn compile exec:java`; use `--ui` para abrir a interface Swing após a execução.
