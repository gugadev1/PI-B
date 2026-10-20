# Evidências dos Testes — TechNova

## Ambiente e execução

- Data da validação: 19/10/2026.
- Java: Eclipse Temurin 17.0.20.1.
- Maven: 3.9.11.
- Persistência de integração: H2 em memória; inicialização local também foi verificada com o banco H2 em arquivo padrão.
- Suíte JUnit e empacotamento:

```text
mvn -B clean verify
Tests run: 2, Failures: 0, Errors: 0 — CarrinhoServiceTest
Tests run: 4, Failures: 0, Errors: 0 — TechNovaApiControllerTest
Tests run: 6, Failures: 0, Errors: 0
BUILD SUCCESS
```

O comando gerou `target/pi2-technova-refatorado-2.0.0.jar` como artefato executável Spring Boot. Também foi executada a suíte manual legada:

```text
mvn -B compile exec:java
RESULTADO FINAL: 16/16 testes aprovados com sucesso!
BUILD SUCCESS
```

## Resultados por cenário

- **CT01:** autenticação válida, senha inválida (`401`), recurso protegido sem sessão (`401`) e encerramento de sessão (`204`).
- **CT02:** funcionário cria e edita produto; dados inválidos (`400`), produto inexistente (`404`), cliente sem acesso (`403`) e funcionário sem permissão para ativar/inativar (`403`).
- **CT03:** cliente criado com senha PBKDF2 consegue autenticar; e-mail inválido retorna `400`.
- **CT04:** os dois testes unitários do carrinho passaram, incluindo consolidação, total e rejeição de quantidade acima do estoque sem mutação.
- **CT05–CT08:** pedido é persistido com status inicial e total calculado; estoque é debitado; tentativa acima do estoque retorna `409` sem alterar o saldo; leitura e atualização são conferidas no H2.
- **CT06–CT07:** clientes não criam pedidos para outro cadastro; funcionário não altera status; administrador aprova e cancela; transição posterior inválida retorna `409`; cancelamento devolve o estoque sem permitir estorno repetido.
- **CT09:** o navegador abriu a página e os recursos estáticos servidos pelo Spring MVC. No fluxo real, cliente autenticou, viu somente produtos ativos, adicionou item ao carrinho e confirmou pedido; administrador viu o pedido, cancelou-o e a interface confirmou o estorno. O histórico de outro cliente permaneceu vazio. O mesmo artefato inicializou também usando a configuração padrão de H2 em arquivo.

## Bugtracking e correção

O primeiro teste de criação de pedido reproduziu HTTP `500`: o H2 retornou as colunas geradas `ID` e `DATA_CRIACAO`, mas a implementação solicitava uma chave única. A falha foi registrada no GitHub Issues: [#3 — Criação de pedido retorna 500 ao receber mais de uma chave gerada pelo H2](https://github.com/gugadev1/PI-B/issues/3). A persistência passou a solicitar explicitamente a coluna `ID`; o issue foi fechado após a regressão e a suíte completa passarem.

Reexecução da regressão após a correção:

```text
mvn -B -Dtest=TechNovaApiControllerTest#criaPedidoComEstoqueTransacionalEFiltraPedidosDoCliente test
Tests run: 1, Failures: 0, Errors: 0
BUILD SUCCESS
```

A execução limpa completa posterior passou com 6/6 testes JUnit e 16/16 verificações do núcleo. Não foram usados nem alegados screenshots ou testes em MySQL; essa integração não foi executada porque não havia servidor MySQL configurado.
