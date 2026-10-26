# Nucita Lanches · JavaFX

Interface desktop de atendimento da Nucita Lanches, com a identidade visual da marca e integração às operações Java já implementadas no projeto [Nucita-Lanches/Nucita-Lanches](https://github.com/Nucita-Lanches/Nucita-Lanches).

O projeto integrado está em **[`outputs/`](outputs/)**. Abra o [`pom.xml`](outputs/pom.xml) dessa pasta na IDE.

![Tela JavaFX da Nucita com dados de exemplo](outputs/previa-nucita.png)

## Funcionalidades disponíveis

- Cadastro e listagem de produtos, com nome e preço.
- Busca de produtos pelo nome, ignorando acentos.
- Cadastro, consulta e seleção de mesas com número positivo e único.
- Criação de pedidos vinculados a mesas cadastradas.
- Inclusão de produtos com quantidade positiva.
- Consulta dos itens e seleção de um pedido para continuar o atendimento.
- Vários pedidos associados à mesma mesa.

Os dados ficam **em memória durante a execução** e são apagados ao fechar. Total, subtotal, remoção de itens e finalização ainda não estão implementados no código de negócio incorporado.

## Executar

Requisitos: **JDK 21**, Maven ou IntelliJ com Maven, e uma sessão gráfica.

Na pasta desta versão do projeto:

```sh
cd outputs
mvn javafx:run
```

No IntelliJ, abra `outputs/pom.xml`, carregue as dependências Maven e execute o objetivo `javafx:run`.

No Windows, depois de carregar as dependências JavaFX 21.0.6, também é possível abrir [`outputs/Executar-Nucita.cmd`](outputs/Executar-Nucita.cmd). Esse iniciador usa Java e as dependências já presentes no cache local do Maven.

## Testes

```sh
cd outputs
mvn test
```

São **14 testes**: 13 testes de domínio/aplicação e um teste de integração JavaFX que exercita os botões e formulários reais. O teste visual requer uma sessão gráfica e verifica também o cardápio preenchido e a janela de 1220 × 730. A captura acima contém dados de exemplo criados somente no processo de teste.

## Estrutura

```text
outputs/
├── pom.xml
├── Executar-Nucita.cmd
├── Executar-Nucita.ps1
├── TEMA-JAVAFX.md
└── src/
    ├── main/
    │   ├── java/br/com/nucitalanches/preview/   # Interface e ícones
    │   ├── java/com/senai/nucitalanches/nucitalanches/
    │   │   ├── application/                   # Operações de Restaurante
    │   │   ├── domain/                        # Produto, Mesa, Pedido e itens
    │   │   └── storage/                       # Armazenamento em memória
    │   └── resources/                         # FXML, CSS e imagens
    └── test/java/                             # Testes de domínio e interface
```

`outputs1/` contém outra versão da interface, mantida como referência. Os arquivos `build/` e `target/` são gerados durante a execução e não fazem parte dos novos commits do projeto integrado.

## Origem da integração

As classes de negócio foram incorporadas de `Nucita-Lanches/Nucita-Lanches` no commit [`f30e1d1c86a10699b87a8db29fc2b1498f81faf6`](https://github.com/Nucita-Lanches/Nucita-Lanches/commit/f30e1d1c86a10699b87a8db29fc2b1498f81faf6). A integração usa chamadas Java locais; não há uma API de servidor nem sincronização automática com o GitHub.

Os detalhes de uso, arquitetura e refinamento visual estão em [`outputs/TEMA-JAVAFX.md`](outputs/TEMA-JAVAFX.md).
