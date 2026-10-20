# TechNova Soluções

Aplicação web Java 17 que integra o front-end existente a uma API Spring MVC e a um banco relacional. O núcleo de regras de negócio também continua disponível para a interface desktop e para os testes unitários.

## Executar localmente

Requisitos: JDK 17 e Maven 3.6 ou superior.

```powershell
mvn spring-boot:run
```

Abra `http://localhost:8080`. A aplicação cria o banco H2 local em `data/technova.mv.db` e inicializa contas e catálogo de demonstração na primeira execução. Os dados permanecem entre reinicializações; remova o arquivo do banco somente se quiser reiniciar a demonstração.

| Perfil | E-mail | Senha inicial |
| --- | --- | --- |
| Administrador | `admin@technova.com` | `admin123` |
| Funcionário | `vendas@technova.com` | `vendas123` |
| Cliente | `cliente.balcao@technova.com` | `123456` |
| Cliente alternativo | `bruno@email.com` | `bruno123` |

As contas são apenas para desenvolvimento e não devem ser expostas em produção. Senhas criadas pelo cadastro são armazenadas com PBKDF2. Para configurar a conexão, defina `TECHNOVA_DB_URL`, `TECHNOVA_DB_USER`, `TECHNOVA_DB_PASSWORD` e `TECHNOVA_DB_DRIVER`; o padrão local é H2. O esquema fornecido em `src/main/resources/schema.sql` é destinado ao H2.

## Funcionalidades e permissões

- Administradores e funcionários podem consultar, cadastrar e editar produtos e clientes; somente administradores ativam/inativam produtos e alteram ou cancelam pedidos.
- Clientes consultam apenas produtos ativos e o próprio histórico, e criam pedidos somente para o próprio cadastro.
- Pedidos validam status, disponibilidade e quantidade; a baixa de estoque, os itens e o pedido são gravados dentro de uma transação. Cancelamentos permitidos estornam o estoque.
- A sessão HTTP usa cookie `HttpOnly` e `SameSite=Lax`; os erros da API retornam códigos HTTP coerentes sem expor detalhes internos de persistência.

## Organização

- `com.technova.web`: controladores e tratamento de erros Spring MVC.
- `com.technova.repository.jdbc`: implementação JDBC relacional usada pela aplicação.
- `com.technova.repository.memory` e `com.technova.repository.mysql`: implementações existentes para testes e compatibilidade.
- `com.technova.service`, `domain` e `validation`: regras de negócio e validações.
- `src/main/resources/static`: interface HTML/CSS/JavaScript servida pelo Spring Boot e integrada à API `/api`.
- `com.technova.app.TechNovaApp`: verificações manuais preexistentes do núcleo/desktop.

## Testes e documentação

Execute `mvn test` para os testes do núcleo e da API Spring MVC com H2 em memória. A aplicação também pode ser empacotada com `mvn package`. Os resultados dos casos do plano e as evidências de versionamento desta etapa estão em `docs/evidencias_testes.md` e `docs/evidencias_versionamento.md`; o plano atualizado está em `docs/plano_testes.md`.
