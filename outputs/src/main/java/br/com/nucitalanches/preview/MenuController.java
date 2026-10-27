package br.com.nucitalanches.preview;

import com.senai.nucitalanches.nucitalanches.application.Restaurante;
import com.senai.nucitalanches.nucitalanches.domain.*;
import com.senai.nucitalanches.nucitalanches.storage.BancoDeDados;
import javafx.event.ActionEvent;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/** Liga a interface às operações já implementadas no projeto Nucita Lanches. */
public class MenuController {
    @FXML private TextField searchField;
    @FXML private TilePane productGrid;
    @FXML private HBox searchBox;
    @FXML private VBox cartItems, emptyState;
    @FXML private Label emptyLabel, countLabel, tableLabel, statusLabel, orderTitle, nextTableLabel;
    @FXML private Label catalogCountLabel, catalogHintLabel, orderStatusLabel, emptyTitle;
    @FXML private Button newOrderButton, emptyActionButton, menuNav, productsNav, tablesNav, ordersNav;
    private final Restaurante restaurante = new Restaurante();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private Mesa selectedMesa;
    private Pedido activeOrder;

    @FXML private void initialize() {
        menuNav.setGraphic(UiIcons.icon("menu"));
        productsNav.setGraphic(UiIcons.icon("products"));
        tablesNav.setGraphic(UiIcons.icon("tables"));
        ordersNav.setGraphic(UiIcons.icon("orders"));
        newOrderButton.setGraphic(UiIcons.icon("plus"));
        newOrderButton.setGraphicTextGap(10);
        searchField.textProperty().addListener((obs, old, value) -> renderProducts());
        searchField.focusedProperty().addListener((obs, old, focused) -> searchBox.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused-within"), focused));
        productGrid.widthProperty().addListener((obs, old, value) -> sizeProductCards());
        renderProducts();
        renderOrder();
        statusLabel.setText("Cadastre produtos e mesas para começar o atendimento.");
    }
    private Label label(String text, String style) {
        Label result = new Label(text);
        result.getStyleClass().add(style);
        result.setWrapText(true);
        return result;
    }
    private Region spacer() {
        Region region = new Region(); HBox.setHgrow(region, Priority.ALWAYS); return region;
    }
    private String money(double price) { return currency.format(price); }
    private String mesaName(Mesa mesa) { return "Mesa " + mesa.getNumero(); }
    private String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
    private String initials(String name) {
        String[] words = name.trim().split("\\s+");
        String first = Character.toString(words[0].codePointAt(0));
        return (first + (words.length > 1 ? Character.toString(words[words.length - 1].codePointAt(0)) : "")).toUpperCase(Locale.of("pt", "BR"));
    }
    private void sizeProductCards() {
        double available = productGrid.getWidth();
        if (available <= 0) return;
        int columns = Math.min(3, Math.max(1, (int) ((available + 18) / 228)));
        double width = Math.floor((available - (columns - 1) * 18) / columns);
        productGrid.setPrefTileWidth(width);
        for (Node child : productGrid.getChildren()) if (child instanceof VBox card) {
            card.setMinWidth(width); card.setPrefWidth(width); card.setMaxWidth(width);
        }
    }
    private void renderProducts() {
        productGrid.getChildren().clear();
        String query = normalize(searchField.getText().trim());
        for (Produto produto : restaurante.listarProdutos()) {
            if (!normalize(produto.getNome()).contains(query)) continue;
            Circle ring = new Circle(43); ring.getStyleClass().add("product-art-ring");
            Label monogram = label(initials(produto.getNome()), "product-monogram");
            Label mark = label("NUCITA LANCHES", "product-art-caption");
            StackPane.setAlignment(mark, Pos.BOTTOM_CENTER); StackPane.setMargin(mark, new Insets(0, 0, 12, 0));
            StackPane image = new StackPane(ring, monogram, mark);
            image.getStyleClass().add("product-art");
            int variant = (int) (produto.getId() % 3);
            if (variant == 1) image.getStyleClass().add("product-art-dark");
            else if (variant == 2) image.getStyleClass().add("product-art-rose");
            Button add = new Button("Adicionar", UiIcons.icon("plus"));
            add.setGraphicTextGap(5);
            add.setId("addProduct-" + produto.getId());
            add.getStyleClass().add("add-button");
            add.setAccessibleText("Adicionar " + produto.getNome() + " ao pedido");
            add.setDisable(activeOrder == null);
            add.setTooltip(new Tooltip(activeOrder == null ? "Selecione uma mesa e crie um pedido primeiro." : "Adicionar ao pedido " + activeOrder.getNumeroPedido()));
            add.setOnAction(event -> showAddItem(produto));
            VBox pricing = new VBox(3, label(money(produto.getPreco()), "product-price"), label("por unidade", "unit-caption"));
            HBox footer = new HBox(8, pricing, spacer(), add);
            footer.setAlignment(Pos.CENTER_LEFT);
            Label name = label(produto.getNome(), "product-name"); name.setMinHeight(40); name.setMaxHeight(40);
            name.setTooltip(new Tooltip(produto.getNome()));
            VBox body = new VBox(9, label("PRODUTO " + String.format("%02d", produto.getId()), "product-description"), name, footer);
            body.getStyleClass().add("product-body");
            VBox card = new VBox(image, body); card.setPrefWidth(220); card.setMaxWidth(220);
            card.getStyleClass().add("product-card"); productGrid.getChildren().add(card);
        }
        boolean empty = productGrid.getChildren().isEmpty();
        int count = restaurante.listarProdutos().size();
        catalogCountLabel.setText(count == 0 ? "Seu cardápio começa aqui." : count + (count == 1 ? " produto preparado para o atendimento." : " produtos preparados para o atendimento."));
        catalogHintLabel.setText(activeOrder == null ? "Abra um pedido para adicionar itens." : "Adicionando ao pedido #" + activeOrder.getNumeroPedido());
        emptyTitle.setText(count == 0 ? "Seu cardápio começa aqui." : "Nenhum sabor por aqui.");
        emptyLabel.setText(restaurante.listarProdutos().isEmpty()
                ? "Cadastre seu primeiro produto e prepare a casa para receber novos pedidos."
                : "Tente outro nome para encontrar o que você procura.");
        emptyLabel.setVisible(empty); emptyLabel.setManaged(empty);
        emptyState.setVisible(empty); emptyState.setManaged(empty);
        emptyActionButton.setText(count == 0 ? "Cadastrar primeiro produto" : "Ver todo o cardápio");
        emptyActionButton.setOnAction(event -> { if (count == 0) showProducts(); else showMenu(); });
        productGrid.setManaged(!empty);
        sizeProductCards();
    }
    private void renderOrder() {
        cartItems.getChildren().clear();
        orderTitle.setText(activeOrder == null ? "Seu pedido" : "Pedido #" + activeOrder.getNumeroPedido());
        tableLabel.setText(activeOrder == null ? "Nenhum pedido aberto" : mesaName(activeOrder.getMesa()));
        nextTableLabel.setText(selectedMesa == null ? "Selecione uma mesa para criar um pedido." : "Próximo pedido: " + mesaName(selectedMesa));
        newOrderButton.setDisable(selectedMesa == null);
        orderStatusLabel.setText(activeOrder == null ? "Aguardando" : "Aberto");
        orderStatusLabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("active"), activeOrder != null);
        long count = 0;
        if (activeOrder != null) {
            for (ItemPedido item : activeOrder.getItens()) {
                count += item.getQuantidade();
                VBox text = new VBox(6, label(item.getProduto().getNome(), "cart-item-name"),
                        label(money(item.getProduto().getPreco()) + " / un.", "cart-price"));
                HBox.setHgrow(text, Priority.ALWAYS);
                Label avatar = label(initials(item.getProduto().getNome()), "item-avatar");
                Label quantity = label(item.getQuantidade() + "×", "item-quantity");
                HBox line = new HBox(10, avatar, text, quantity); line.setAlignment(Pos.CENTER_LEFT);
                VBox row = new VBox(line);
                row.getStyleClass().add("order-item"); cartItems.getChildren().add(row);
            }
        }
        if (cartItems.getChildren().isEmpty()) {
            StackPane symbol = new StackPane(UiIcons.icon("orders")); symbol.getStyleClass().add("empty-symbol"); symbol.setMaxSize(54, 54); symbol.setMinSize(54, 54);
            VBox empty = new VBox(symbol, label(activeOrder == null ? "Tudo começa na mesa." : "Uma boa escolha vem aí.", "order-empty-title"),
                    label(activeOrder == null ? "Selecione uma mesa e abra um novo pedido para começar." : "Escolha um produto no cardápio e adicione ao pedido.", "order-empty-copy"));
            empty.getStyleClass().add("order-empty"); cartItems.getChildren().add(empty);
        }
        countLabel.setText(Long.toString(count));
    }
    private <T> Dialog<T> dialog(String title) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle("Nucita Lanches • " + title);
        dialog.initOwner(searchField.getScene().getWindow());
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/javafx-nucita-theme.css")).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("management-dialog");
        VBox header = new VBox(7, label("NUCITA / ATENDIMENTO", "dialog-eyebrow"), label(title, "dialog-title"),
                label(switch (title) {
                    case "Produtos" -> "Os sabores da casa, organizados em um só lugar.";
                    case "Mesas" -> "Prepare o próximo atendimento e escolha onde servir.";
                    case "Pedidos" -> "Cada mesa, cada escolha. Continue de onde parou.";
                    default -> "Mais um favorito para este pedido.";
                }, "dialog-copy"));
        header.getStyleClass().add("dialog-header");
        dialog.getDialogPane().setHeader(header);
        Button active = switch (title) { case "Produtos" -> productsNav; case "Mesas" -> tablesNav; case "Pedidos" -> ordersNav; default -> menuNav; };
        for (Button nav : java.util.List.of(menuNav, productsNav, tablesNav, ordersNav)) {
            nav.getStyleClass().remove("nav-active");
            if (nav == active) nav.getStyleClass().add("nav-active");
        }
        dialog.setOnHidden(event -> {
            for (Button nav : java.util.List.of(productsNav, tablesNav, ordersNav)) nav.getStyleClass().remove("nav-active");
            if (!menuNav.getStyleClass().contains("nav-active")) menuNav.getStyleClass().add("nav-active");
        });
        dialog.setResizable(true); return dialog;
    }
    private <T> void configureList(ListView<T> list, Function<T, Node> display) {
        list.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                Node graphic = empty || item == null ? null : display.apply(item);
                if (graphic instanceof Region row) row.prefWidthProperty().bind(list.widthProperty().subtract(16));
                setGraphic(graphic);
            }
        });
        list.setPrefHeight(315);
        list.setMinHeight(180);
    }
    private HBox recordRow(Node marker, String title, String subtitle, Node end) {
        Label name = label(title, "record-name");
        VBox text = new VBox(5, name, label(subtitle, "record-meta"));
        text.setMinWidth(0); HBox.setHgrow(text, Priority.ALWAYS);
        HBox row = new HBox(11, marker, text);
        if (end != null) row.getChildren().add(end);
        row.setAlignment(Pos.CENTER_LEFT); row.getStyleClass().add("record-row");
        return row;
    }
    private VBox content(Node... nodes) {
        VBox box = new VBox(15, nodes); box.getStyleClass().add("dialog-content"); box.setPrefWidth(510); return box;
    }
    private VBox form(Node... nodes) {
        VBox box = new VBox(14, nodes); box.getStyleClass().add("form-panel"); box.setMinWidth(280); return box;
    }
    private VBox field(String caption, TextField input) {
        Label name = label(caption, "field-label"); name.setLabelFor(input);
        return new VBox(7, name, input);
    }
    private HBox splitContent(VBox left, VBox right) {
        left.setPrefWidth(365); left.setMinWidth(300);
        right.setPrefWidth(305);
        HBox.setHgrow(left, Priority.ALWAYS); HBox.setHgrow(right, Priority.ALWAYS);
        HBox box = new HBox(22, left, right);
        box.getStyleClass().add("dialog-content"); box.setPrefWidth(790); box.setAlignment(Pos.TOP_LEFT);
        return box;
    }
    private Label feedback(String id) {
        Label feedback = label("", "form-feedback"); feedback.setId(id);
        feedback.visibleProperty().bind(feedback.textProperty().isNotEmpty());
        feedback.managedProperty().bind(feedback.visibleProperty());
        return feedback;
    }
    private void action(Label feedback, Runnable operation) {
        feedback.getStyleClass().remove("feedback-error");
        try { operation.run(); }
        catch (IllegalArgumentException exception) { feedback.getStyleClass().add("feedback-error"); feedback.setText(exception.getMessage()); }
    }
    private int integer(String value, String field) {
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException(field + ": informe um número inteiro válido."); }
    }
    @FXML private void showMenu() { searchField.clear(); renderProducts(); }

    @FXML private void showProducts() {
        Dialog<Void> dialog = dialog("Produtos");
        dialog.getDialogPane().setId("productsDialog");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ListView<Produto> list = new ListView<>(); list.setId("productsList");
        configureList(list, produto -> recordRow(label(initials(produto.getNome()), "record-number"), produto.getNome(),
                "Produto #" + produto.getId(), label(money(produto.getPreco()), "record-value")));
        list.getItems().setAll(restaurante.listarProdutos()); list.setPlaceholder(new Label("Nenhum produto cadastrado."));
        TextField name = new TextField(); name.setId("productName"); name.setPromptText("Nome do produto");
        TextField price = new TextField(); price.setId("productPrice"); price.setPromptText("Ex.: 12,50");
        Label feedback = feedback("productFeedback");
        Button save = new Button("Cadastrar produto"); save.setId("saveProduct"); save.getStyleClass().add("hero-button");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(event -> action(feedback, () -> {
            String value = price.getText().trim();
            if (!value.matches("[0-9]+([.,][0-9]{1,2})?")) throw new IllegalArgumentException("Informe o preço sem separador de milhar, com até duas casas decimais. Ex.: 12,50.");
            Produto produto = new Produto(name.getText(), new BigDecimal(value.replace(',', '.')).doubleValue());
            restaurante.cadastroProduto(produto); list.getItems().setAll(restaurante.listarProdutos());
            name.clear(); price.clear(); feedback.setText("Produto cadastrado: " + produto.getNome() + ".");
            statusLabel.setText("Produto cadastrado no cardápio."); renderProducts(); name.requestFocus();
        }));
        VBox catalog = new VBox(10, label("Produtos cadastrados", "panel-heading"), list,
                label("O cardápio é atualizado assim que você cadastra um produto.", "form-hint"));
        dialog.getDialogPane().setContent(splitContent(catalog, form(label("Adicionar ao cardápio", "panel-heading"),
                label("Um nome, um preço e mais um motivo para voltar.", "form-hint"), field("Nome do produto", name),
                field("Preço por unidade · R$", price), label("Use vírgula ou ponto para os centavos.", "form-hint"), feedback, save)));
        dialog.showAndWait();
    }
    @FXML private void showTables() {
        Dialog<Mesa> dialog = dialog("Mesas"); dialog.getDialogPane().setId("tablesDialog");
        ButtonType select = new ButtonType("Usar no próximo pedido", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(select, ButtonType.CANCEL);
        ListView<Mesa> list = new ListView<>(); list.setId("tablesList");
        configureList(list, mesa -> recordRow(label(String.format("%02d", mesa.getNumero()), "record-number"),
                mesaName(mesa), "Pronta para receber um pedido", UiIcons.icon("arrow")));
        list.getItems().setAll(restaurante.listarMesas()); list.setPlaceholder(new Label("Nenhuma mesa cadastrada."));
        if (selectedMesa != null) list.getSelectionModel().select(selectedMesa);
        TextField number = new TextField(); number.setId("tableNumber"); number.setPromptText("Número da mesa");
        Label feedback = feedback("tableFeedback");
        Button save = new Button("Cadastrar mesa"); save.setId("saveTable"); save.getStyleClass().add("hero-button");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(event -> action(feedback, () -> {
            Mesa mesa = new Mesa(integer(number.getText(), "Mesa"));
            if (!restaurante.cadastrarMesa(mesa)) throw new IllegalArgumentException("O número deve ser positivo e não pode pertencer a uma mesa já cadastrada.");
            list.getItems().setAll(restaurante.listarMesas()); list.getSelectionModel().select(mesa);
            feedback.setText(mesaName(mesa) + " cadastrada. Selecione Usar no próximo pedido para continuar."); number.clear();
        }));
        Button find = new Button("Consultar número"); find.setId("findTable");
        find.getStyleClass().add("secondary-button"); find.setMaxWidth(Double.MAX_VALUE);
        find.setOnAction(event -> action(feedback, () -> {
            Mesa mesa = restaurante.consultarMesaPorNumero(integer(number.getText(), "Mesa"));
            if (mesa == null) throw new IllegalArgumentException("Mesa não encontrada.");
            list.getSelectionModel().select(mesa); list.scrollTo(mesa); feedback.setText(mesaName(mesa) + " encontrada.");
        }));
        Button selectButton = (Button) dialog.getDialogPane().lookupButton(select); selectButton.setId("selectTable");
        selectButton.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        dialog.setResultConverter(button -> button == select ? list.getSelectionModel().getSelectedItem() : null);
        dialog.getDialogPane().setContent(splitContent(new VBox(10, label("Escolha uma mesa", "panel-heading"), list,
                label("Selecione na lista para usar no próximo pedido.", "form-hint")),
                form(label("Organize o atendimento", "panel-heading"), label("Cadastre uma nova mesa ou encontre uma pelo número.", "form-hint"),
                        field("Número da mesa", number), label("Use um número inteiro, positivo e ainda não cadastrado.", "form-hint"), save, find, feedback)));
        dialog.showAndWait().ifPresent(mesa -> {
            selectedMesa = mesa; renderOrder(); statusLabel.setText(mesaName(mesa) + " selecionada para o próximo pedido.");
        });
    }
    @FXML private void createOrder() {
        if (selectedMesa == null) return;
        try {
            activeOrder = restaurante.criarPedido(selectedMesa.getNumero()); renderOrder(); renderProducts();
            statusLabel.setText("Pedido #" + activeOrder.getNumeroPedido() + " aberto. Adicione produtos pelo cardápio.");
        } catch (IllegalArgumentException exception) { statusLabel.setText(exception.getMessage()); }
    }
    private void showAddItem(Produto produto) {
        if (activeOrder == null) return;
        Dialog<Void> dialog = dialog("Adicionar item"); dialog.getDialogPane().setId("addItemDialog");
        ButtonType add = new ButtonType("Adicionar ao pedido", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(add, ButtonType.CANCEL);
        TextField quantity = new TextField("1"); quantity.setId("itemQuantity");
        Label feedback = feedback("itemFeedback");
        HBox summary = recordRow(label(initials(produto.getNome()), "record-number"), produto.getNome(), "Preço por unidade", label(money(produto.getPreco()), "record-value"));
        dialog.getDialogPane().setContent(content(summary,
                label("Pedido #" + activeOrder.getNumeroPedido() + " · " + mesaName(activeOrder.getMesa()), "form-hint"),
                field("Quantas unidades vamos servir?", quantity), feedback));
        Button save = (Button) dialog.getDialogPane().lookupButton(add); save.setId("saveItem");
        save.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                restaurante.adicionarItemAoPedido(activeOrder.getNumeroPedido(), produto, integer(quantity.getText(), "Quantidade"));
                renderOrder(); statusLabel.setText(produto.getNome() + " adicionado ao pedido #" + activeOrder.getNumeroPedido() + ".");
            } catch (IllegalArgumentException exception) { feedback.getStyleClass().remove("feedback-error"); feedback.getStyleClass().add("feedback-error"); feedback.setText(exception.getMessage()); event.consume(); }
        });
        dialog.showAndWait();
    }
    @FXML private void showOrders() {
        Dialog<Pedido> dialog = dialog("Pedidos"); dialog.getDialogPane().setId("ordersDialog");
        ButtonType select = new ButtonType("Continuar pedido", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(select, ButtonType.CANCEL);
        ListView<Pedido> list = new ListView<>(); list.setId("ordersList");
        configureList(list, pedido -> recordRow(UiIcons.icon("orders"), "Pedido #" + pedido.getNumeroPedido(), mesaName(pedido.getMesa()),
                label(pedido.getStatus() == StatusPedido.ABERTO ? "Aberto" : "Finalizado", "record-state")));
        list.getItems().setAll(BancoDeDados.getInstancia().listarPedidos()); list.setPlaceholder(new Label("Nenhum pedido criado nesta sessão."));
        TextArea details = new TextArea(); details.setId("orderDetails"); details.setEditable(false); details.setWrapText(true); details.setPrefRowCount(7);
        list.getSelectionModel().selectedItemProperty().addListener((obs, old, pedido) -> {
            if (pedido == null) { details.clear(); return; }
            StringBuilder text = new StringBuilder("Pedido #" + pedido.getNumeroPedido() + " • " + mesaName(pedido.getMesa()) + "\nStatus: " + pedido.getStatus() + "\n\n");
            if (pedido.getItens().isEmpty()) text.append("Nenhum item adicionado.");
            for (ItemPedido item : pedido.getItens()) text.append(item.getQuantidade()).append(" × ").append(item.getProduto().getNome())
                    .append(" • preço unitário: ").append(money(item.getProduto().getPreco())).append('\n');
            details.setText(text.toString());
        });
        Button selectButton = (Button) dialog.getDialogPane().lookupButton(select); selectButton.setId("selectOrder");
        selectButton.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        dialog.setResultConverter(button -> button == select ? list.getSelectionModel().getSelectedItem() : null);
        details.setPrefWidth(290); details.setMinWidth(0);
        VBox summary = form(label("Resumo do pedido", "panel-heading"), details);
        VBox.setVgrow(details, Priority.ALWAYS);
        summary.setMinHeight(340);
        dialog.getDialogPane().setContent(splitContent(new VBox(10, label("Pedidos desta sessão", "panel-heading"), list,
                label("Selecione um pedido para consultar ou continuar.", "form-hint")), summary));
        if (activeOrder != null) list.getSelectionModel().select(activeOrder);
        dialog.showAndWait().ifPresent(pedido -> {
            activeOrder = pedido; selectedMesa = pedido.getMesa(); renderOrder(); renderProducts();
            statusLabel.setText("Pedido #" + pedido.getNumeroPedido() + " selecionado para incluir itens.");
        });
    }
}
