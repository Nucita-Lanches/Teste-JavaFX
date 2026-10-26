# Nucita Lanches — atendimento JavaFX

A interface local usa as classes de negócio de https://github.com/Nucita-Lanches/Nucita-Lanches, versão `f30e1d1c86a10699b87a8db29fc2b1498f81faf6` da branch `main`, consultada em 26/10/2026. As classes de `application`, `domain` e `storage` foram incorporadas sem alteração em `src/main/java/com/senai/nucitalanches/nucitalanches`.

A integração é feita por chamadas Java locais a `Restaurante` e pela consulta ao armazenamento `BancoDeDados`. O repositório de origem não fornece uma API de servidor. A execução da interface não depende de conexão com o GitHub. Atualizações futuras do código de origem precisam ser incorporadas ao projeto.

## Executar

Neste computador, abra `Executar-Nucita.cmd`. O script compila os fontes e abre o atendimento com Java 21 e JavaFX 21.0.6 já presentes no cache do Maven.

No IntelliJ, abra `pom.xml` como projeto Maven e execute `Plugins → javafx → javafx:run`.

## Usar

1. Abra **Produtos**, informe nome e preço e clique em **Cadastrar produto**. A listagem e o cardápio mostram os produtos efetivamente cadastrados. Use vírgula ou ponto para os centavos, sem separador de milhar.
2. Abra **Mesas** para cadastrar, listar ou consultar pelo número. O cadastro usa a validação original: número inteiro positivo e único.
3. Selecione a mesa e clique em **Usar no próximo pedido**.
4. Clique em **Novo pedido**. O número é atribuído pelo armazenamento original e o pedido começa **ABERTO**, vinculado à mesa escolhida.
5. No cardápio, clique em **+**, informe a quantidade e confirme **Adicionar ao pedido**. A operação usa `Restaurante.adicionarItemAoPedido` e suas validações. Cada inclusão gera um item, como no código de origem.
6. Abra **Pedidos** para consultar os itens e escolher **Continuar pedido**. Novas inclusões passam a pertencer ao pedido selecionado.

Uma mesa pode ter vários pedidos. Selecionar outra mesa prepara o próximo pedido e mantém o vínculo do pedido atual.

## Funções conectadas

| Interface | Operação original |
| --- | --- |
| Cadastro de produto | `Restaurante.cadastroProduto` |
| Listagem e busca no cardápio | `Restaurante.listarProdutos` |
| Cadastro de mesa | `Restaurante.cadastrarMesa` |
| Consulta e listagem de mesas | `Restaurante.consultarMesaPorNumero` e `listarMesas` |
| Novo pedido | `Restaurante.criarPedido` |
| Inclusão de produto e quantidade | `Restaurante.adicionarItemAoPedido` |
| Consulta dos pedidos e seus itens | `BancoDeDados.listarPedidos` e `Pedido.getItens` |

## Estado dos dados

O armazenamento original é um singleton em memória: produtos, mesas e pedidos são apagados quando o programa fecha. A aplicação inicia sem cadastros de demonstração. As imagens dos produtos usam a marca como ilustração, pois o modelo original contém apenas nome, preço e identificador.

O repositório ainda não implementa total/subtotal, remoção de itens ou finalização de pedidos. A interface apresenta preços unitários, quantidades e o status original, sem oferecer essas operações pendentes. Os controles antigos de confirmação e remoção da prévia foram substituídos pelo fluxo implementado.

## Estrutura

- `src/main/java/br/com/nucitalanches/preview/MenuController.java`: formulários e integração com o domínio.
- `src/main/java/br/com/nucitalanches/preview/NucitaPreviewApp.java`: entrada JavaFX mantida para compatibilidade com os comandos existentes.
- `src/main/java/com/senai/nucitalanches/nucitalanches/application`: operações originais de `Restaurante`.
- `src/main/java/com/senai/nucitalanches/nucitalanches/domain`: modelos originais.
- `src/main/java/com/senai/nucitalanches/nucitalanches/storage`: armazenamento original em memória.
- `src/main/resources/view/exemplo-cardapio.fxml`: tela carregada pela aplicação.
- `src/main/resources/css/javafx-nucita-theme.css`: tema visual.
- `src/test/java`: 13 testes de domínio/aplicação importados do repositório e 1 teste de integração da interface.

As cópias de FXML e CSS na raiz foram sincronizadas como referência. A aplicação carrega os arquivos em `src/main/resources`.

## Validação

Em 26/10/2026, a compilação e os **14 testes** passaram. O teste JavaFX abre a tela e usa os botões e formulários reais para verificar cadastro/listagem de produtos, busca sem acentos, validação de preço/nome, cadastro/consulta de mesas, duplicidade, criação de pedidos, quantidades inválidas, associação de mesa, seleção de pedido e múltiplos pedidos na mesma mesa. Os dados desses testes existem apenas no processo de teste.

Para repetir com Maven configurado: `mvn test`. Pelo IntelliJ, execute o objetivo Maven `test`. O teste JavaFX requer uma sessão gráfica. Os relatórios ficam em `target/surefire-reports` e as capturas em `target/ui-check`. `previa-nucita.png` mostra uma execução de teste com dados ilustrativos.

## Direção visual

Foi mantida a identidade visual local: carvão #000000, vinho #74223A, creme #F6F2EB e avelã #D8AB70, com Segoe UI na interface e Georgia no destaque. A logo e as imagens da prévia anterior continuam em `src/main/resources/images`.
