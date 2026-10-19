# Plano de Testes — TechNova

## Objetivo

Verificar as regras de negócio do núcleo TechNova e orientar a validação dos principais fluxos da interface web prevista para reutilizar esses serviços. O repositório contém atualmente o núcleo Java e uma interface desktop Swing, mas ainda não contém uma camada web; os testes HTTP e de navegador abaixo são critérios para quando essa camada for implementada.

## Escopo e estratégia

- **Testes unitários (JUnit 5):** regras isoladas de domínio e serviços, sem banco de dados. Os repositórios em memória podem ser usados para testar a integração entre serviços.
- **Testes de integração:** validar persistência com MySQL, incluindo transações de pedidos e atualização de estoque. Não fazem parte da suíte unitária.
- **Testes de aceitação web:** executar os fluxos pelo navegador quando houver interface/controladores web, verificando também mensagens de validação e permissões.
- **Dados de teste:** usar usuários com perfis `ADMINISTRADOR`, `FUNCIONARIO` e `CLIENTE`, produtos ativos/inativos com estoque controlado e clientes válidos/inválidos. Restaurar os dados entre cenários.

## Casos de teste

| ID | Requisito/fluxo | Tipo | Cenário e resultado esperado |
| --- | --- | --- | --- |
| CT01 | RF001 — Autenticação | Integração de serviço / aceitação web | Informar e-mail e senha válidos: autenticar o usuário e disponibilizar o sistema conforme o perfil. Credenciais inválidas ou campos vazios: rejeitar o login e informar erro sem iniciar sessão. |
| CT02 | RF002 — Cadastro e manutenção de produtos | Integração de serviço / aceitação web | Cadastrar e atualizar um produto com dados válidos: persistir os dados e exibi-los na listagem. Nome/categoria ausentes, preço ou estoque negativos: rejeitar a operação com validação, sem persistir dados inválidos. |
| CT03 | RF003 — Cadastro de clientes | Integração de serviço / aceitação web | Cadastrar cliente com e-mail válido: atribuir identificador e exibir o registro. E-mail malformado ou campos obrigatórios ausentes: rejeitar o cadastro. |
| CT04 | RF004 — Carrinho e totalização | Unitário (JUnit) | Adicionar o mesmo produto em operações separadas: manter uma linha com quantidades consolidadas e total igual à soma dos subtotais. A tentativa de ultrapassar o estoque deve lançar `RegraNegocioException` sem alterar o carrinho. |
| CT05 | RF005 — Criação de pedido e estoque | Integração / aceitação web | Confirmar carrinho válido: criar pedido em `EM_PROCESSAMENTO`, calcular seu total e baixar do estoque as quantidades pedidas. Carrinho vazio ou estoque insuficiente: rejeitar a criação sem concluir o pedido. |
| CT06 | RF006 — Permissões e transições de status | Integração de serviço / aceitação web | Administrador pode realizar transição permitida, como `EM_PROCESSAMENTO` para `APROVADO`. Cliente não pode alterar status; transições não permitidas devem ser rejeitadas sem modificar o pedido. |
| CT07 | RF007 — Cancelamento e estorno | Integração de serviço / aceitação web | Cancelar pedido em estado que permita cancelamento: definir `CANCELADO` e devolver ao estoque as quantidades dos itens. A operação deve manter pedido e estoque consistentes. |
| CT08 | Persistência MySQL | Integração | Com banco disponível, salvar e consultar produtos, clientes e pedidos; ao ocorrer falha de persistência, reportar erro e não indicar sucesso ao usuário. |
| CT09 | Sessão e navegação web | Aceitação web (camada futura) | Após login, acessar produtos e pedidos conforme o perfil; ao sair, encerrar sessão e exigir autenticação para páginas protegidas. |

## Critérios de aprovação

- Todos os testes unitários e de integração aplicáveis passam; não há alterações de estado após operações rejeitadas.
- O pedido e o estoque permanecem consistentes após confirmação ou cancelamento.
- A interface web apresenta resultado coerente com a operação, bloqueia acessos não autorizados e não expõe mensagens técnicas de persistência.

## Execução dos testes unitários

Na raiz do projeto, executar `mvn test`. Os testes JUnit ficam em `src/test/java`; nenhum teste unitário exige conexão com MySQL.
