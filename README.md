# TechNova Soluções - Projeto Refatorado (SOLID) para Reutilização Web

## Status do Projeto
Concluído (Fase de Refatoração e Preparação para Arquitetura Web - PI2).

## Tecnologias e Padrões Aplicados
- **Java 17** (Projeto Maven / Apache NetBeans)
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

## Testes
- Execute os testes unitários JUnit 5 com `mvn test`.
- Consulte o [plano de testes](docs/plano_testes.md) para os cenários do núcleo de negócio, da persistência e dos fluxos web previstos.
- A classe `com.technova.app.TechNovaApp` também contém verificações manuais executáveis via `mvn compile exec:java`; use `--ui` para abrir a interface Swing após a execução.
