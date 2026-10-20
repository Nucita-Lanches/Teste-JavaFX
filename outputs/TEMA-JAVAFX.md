# Nucita Lanches — prévia JavaFX

Interface desktop independente, inspirada na logo enviada e na paleta vinho, grafite, creme e avelã.

## Executar

- Neste computador: abra `Executar-Nucita.cmd`. Usa Java 21 e as dependências JavaFX já presentes no cache do Maven.
- Pelo IntelliJ: abra `pom.xml` como projeto Maven e execute `Plugins → javafx → javafx:run`.

## O que funciona

Busca por nome e descrição, categorias, inclusão e remoção de unidades, soma de valores, seleção da mesa e confirmação local. A prévia começa com uma sugestão de dois produtos. Os pedidos ficam somente na memória da sessão. Produtos, preços e imagens são ilustrativos; não há integração com o sistema do restaurante.

## Estrutura

- `src/main/resources/view/exemplo-cardapio.fxml`: composição da tela.
- `src/main/resources/css/javafx-nucita-theme.css`: tema.
- `src/main/resources/images/logo.png`: logo preparada a partir da captura enviada.
- `src/main/resources/images/foods.png`: quatro fotos ilustrativas em uma composição.
- `src/main/java/br/com/nucitalanches/preview/MenuController.java`: interações da prévia.

Para integrar, leve as classes e os recursos para o projeto principal, mantenha os caminhos de recursos e conecte o controlador aos serviços da aplicação. As cópias de FXML/CSS na raiz são referências; a aplicação carrega os arquivos em src/main/resources.

## Direção visual e imagens

Paleta: carvão #000000, vinho #74223A, creme #F6F2EB, avelã #D8AB70. Tipos: Segoe UI para interface e Georgia para o destaque.

Imagens preparadas com a ferramenta integrada de geração de imagens:

1. Prompt da logo: extrair somente a arte central da captura, removendo a interface do WhatsApp; manter personagem, casaco vinho/grafite, nome NUCITA LANCHES e coroa sobre fundo preto. A versão foi gerada a partir da captura; o arquivo original da marca pode substituir logo.png.
2. Prompt das fotos: composição quadrada com quatro quadrantes iguais de fotografia de alimentos em fundo creme — brownie com chocolate e avelã, milk-shake de chocolate, cookie com recheio e smash burger; sem textos ou marcas.

## Verificação

Código compilado com Java 21 e JavaFX 21.0.6. FXML carregado e renderizado. Verificados inclusão, remoção, busca sem resultados e filtro por categoria. A captura da interface está em previa-nucita.png.

