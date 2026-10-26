package br.com.nucitalanches.preview;

import com.senai.nucitalanches.nucitalanches.application.Restaurante;
import com.senai.nucitalanches.nucitalanches.domain.*;
import com.senai.nucitalanches.nucitalanches.storage.BancoDeDados;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    @FXML private VBox cartItems;
    @FXML private Label emptyLabel, countLabel, tableLabel, statusLabel, orderTitle, nextTableLabel;
    @FXML private Button newOrderButton;
    private final Restaurante restaurante = new Restaurante();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private Mesa selectedMesa;
    private Pedido activeOrder;
    private Image logo;

    @FXML private void initialize() {
        logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")));
        searchField.textProperty().addListener((obs, old, value) -> renderProducts());
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
    private void renderProducts() {
        productGrid.getChildren().clear();
        String query = normalize(searchField.getText().trim());
        for (Produto produto : restaurante.listarProdutos()) {
            if (!normalize(produto.getNome()).contains(query)) continue;
            ImageView artwork = new ImageView(logo);
            artwork.setFitWidth(150); artwork.setFitHeight(110); artwork.setPreserveRatio(true);
            StackPane image = new StackPane(artwork); image.setMinHeight(124);
            image.getStyleClass().add("product-photo");
            Button add = new Button("+");
            add.setId("addProduct-" + produto.getId());
            add.getStyleClass().add("add-button");
            add.setAccessibleText("Adicionar " + produto.getNome() + " ao pedido");
            add.setDisable(activeOrder == null);
            add.setTooltip(new Tooltip(activeOrder == null ? "Selecione uma mesa e crie um pedido primeiro." : "Adicionar ao pedido " + activeOrder.getNumeroPedido()));
            add.setOnAction(event -> showAddItem(produto));
            HBox footer = new HBox(label(money(produto.getPreco()), "product-price"), spacer(), add);
            footer.setAlignment(Pos.CENTER_LEFT);
            VBox body = new VBox(8, label(produto.getNome(), "product-name"), label("Produto #" + produto.getId(), "product-description"), footer);
            body.getStyleClass().add("product-body");
            VBox card = new VBox(image, body); card.setPrefWidth(188); card.setMaxWidth(188);
            card.getStyleClass().add("product-card"); productGrid.getChildren().add(card);
        }
        boolean empty = productGrid.getChildren().isEmpty();
        emptyLabel.setText(restaurante.listarProdutos().isEmpty()
                ? "Nenhum produto cadastrado. Use Produtos para cadastrar o primeiro item do cardápio."
                : "Nenhum produto encontrado. Tente outra busca.");
        emptyLabel.setVisible(empty); emptyLabel.setManaged(empty);
    }
    private void renderOrder() {
        cartItems.getChildren().clear();
        orderTitle.setText(activeOrder == null ? "Seu pedido" : "Pedido #" + activeOrder.getNumeroPedido());
        tableLabel.setText(activeOrder == null ? "Nenhum pedido aberto" : mesaName(activeOrder.getMesa()));
        nextTableLabel.setText(selectedMesa == null ? "Selecione uma mesa para criar um pedido." : "Próximo pedido: " + mesaName(selectedMesa));
        newOrderButton.setDisable(selectedMesa == null);
        long count = 0;
        if (activeOrder != null) {
            for (ItemPedido item : activeOrder.getItens()) {
                count += item.getQuantidade();
                VBox row = new VBox(7, label(item.getProduto().getNome(), "cart-item-name"),
                        label("Quantidade: " + item.getQuantidade(), "cart-price"),
                        label("Preço unitário: " + money(item.getProduto().getPreco()), "muted-label"));
                row.getStyleClass().add("order-item"); cartItems.getChildren().add(row);
            }
        }
        if (cartItems.getChildren().isEmpty()) cartItems.getChildren().add(label(activeOrder == null
                ? "Selecione uma mesa e clique em Novo pedido."
                : "Pedido aberto. Use + no cardápio para incluir um produto.", "muted-label"));
        countLabel.setText(Long.toString(count));
    }
    private <T> Dialog<T> dialog(String title) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle("Nucita Lanches • " + title);
        dialog.initOwner(searchField.getScene().getWindow());
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/javafx-nucita-theme.css")).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("management-dialog");
        dialog.setResizable(true); return dialog;
    }
    private <T> void configureList(ListView<T> list, Function<T, String> display) {
        list.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty); setText(empty || item == null ? null : display.apply(item));
            }
        });
        list.setPrefHeight(200);
    }
    private VBox content(Node... nodes) {
        VBox box = new VBox(12, nodes); box.setPadding(new Insets(8)); box.setPrefWidth(510); return box;
    }
    private void action(Label feedback, Runnable operation) {
        try { operation.run(); }
        catch (IllegalArgumentException exception) { feedback.setText(exception.getMessage()); }
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
        configureList(list, produto -> "#" + produto.getId() + " • " + produto.getNome() + " • " + money(produto.getPreco()));
        list.getItems().setAll(restaurante.listarProdutos()); list.setPlaceholder(new Label("Nenhum produto cadastrado."));
        TextField name = new TextField(); name.setId("productName"); name.setPromptText("Nome do produto");
        TextField price = new TextField(); price.setId("productPrice"); price.setPromptText("Ex.: 12,50");
        Label feedback = label("", "form-feedback"); feedback.setId("productFeedback");
        Button save = new Button("Cadastrar produto"); save.setId("saveProduct"); save.getStyleClass().add("hero-button");
        save.setOnAction(event -> action(feedback, () -> {
            String value = price.getText().trim();
            if (!value.matches("[0-9]+([.,][0-9]{1,2})?")) throw new IllegalArgumentException("Informe o preço sem separador de milhar, com até duas casas decimais. Ex.: 12,50.");
            Produto produto = new Produto(name.getText(), new BigDecimal(value.replace(',', '.')).doubleValue());
            restaurante.cadastroProduto(produto); list.getItems().setAll(restaurante.listarProdutos());
            name.clear(); price.clear(); feedback.setText("Produto cadastrado: " + produto.getNome() + ".");
            statusLabel.setText("Produto cadastrado no cardápio."); renderProducts(); name.requestFocus();
        }));
        dialog.getDialogPane().setContent(content(label("Produtos cadastrados", "section-title"), list,
                label("Novo produto", "product-name"), label("Nome", "muted-label"), name,
                label("Preço (R$)", "muted-label"), price, feedback, save));
        dialog.showAndWait();
    }
    @FXML private void showTables() {
        Dialog<Mesa> dialog = dialog("Mesas"); dialog.getDialogPane().setId("tablesDialog");
        ButtonType select = new ButtonType("Usar no próximo pedido", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(select, ButtonType.CANCEL);
        ListView<Mesa> list = new ListView<>(); list.setId("tablesList"); configureList(list, this::mesaName);
        list.getItems().setAll(restaurante.listarMesas()); list.setPlaceholder(new Label("Nenhuma mesa cadastrada."));
        if (selectedMesa != null) list.getSelectionModel().select(selectedMesa);
        TextField number = new TextField(); number.setId("tableNumber"); number.setPromptText("Número da mesa");
        Label feedback = label("", "form-feedback"); feedback.setId("tableFeedback");
        Button save = new Button("Cadastrar mesa"); save.setId("saveTable"); save.getStyleClass().add("hero-button");
        save.setOnAction(event -> action(feedback, () -> {
            Mesa mesa = new Mesa(integer(number.getText(), "Mesa"));
            if (!restaurante.cadastrarMesa(mesa)) throw new IllegalArgumentException("O número deve ser positivo e não pode pertencer a uma mesa já cadastrada.");
            list.getItems().setAll(restaurante.listarMesas()); list.getSelectionModel().select(mesa);
            feedback.setText(mesaName(mesa) + " cadastrada. Selecione Usar no próximo pedido para continuar."); number.clear();
        }));
        Button find = new Button("Consultar número"); find.setId("findTable");
        find.setOnAction(event -> action(feedback, () -> {
            Mesa mesa = restaurante.consultarMesaPorNumero(integer(number.getText(), "Mesa"));
            if (mesa == null) throw new IllegalArgumentException("Mesa não encontrada.");
            list.getSelectionModel().select(mesa); list.scrollTo(mesa); feedback.setText(mesaName(mesa) + " encontrada.");
        }));
        Button selectButton = (Button) dialog.getDialogPane().lookupButton(select); selectButton.setId("selectTable");
        selectButton.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        dialog.setResultConverter(button -> button == select ? list.getSelectionModel().getSelectedItem() : null);
        dialog.getDialogPane().setContent(content(label("Mesas cadastradas", "section-title"), list,
                label("Cadastrar ou consultar uma mesa", "product-name"), number, new HBox(10, save, find), feedback));
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
        Label feedback = label("", "form-feedback"); feedback.setId("itemFeedback");
        dialog.getDialogPane().setContent(content(label(produto.getNome(), "section-title"),
                label("Preço unitário: " + money(produto.getPreco()), "product-price"),
                label("Pedido #" + activeOrder.getNumeroPedido() + " • " + mesaName(activeOrder.getMesa()), "muted-label"),
                label("Quantidade", "product-name"), quantity, feedback));
        Button save = (Button) dialog.getDialogPane().lookupButton(add); save.setId("saveItem");
        save.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                restaurante.adicionarItemAoPedido(activeOrder.getNumeroPedido(), produto, integer(quantity.getText(), "Quantidade"));
                renderOrder(); statusLabel.setText(produto.getNome() + " adicionado ao pedido #" + activeOrder.getNumeroPedido() + ".");
            } catch (IllegalArgumentException exception) { feedback.setText(exception.getMessage()); event.consume(); }
        });
        dialog.showAndWait();
    }
    @FXML private void showOrders() {
        Dialog<Pedido> dialog = dialog("Pedidos"); dialog.getDialogPane().setId("ordersDialog");
        ButtonType select = new ButtonType("Continuar pedido", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(select, ButtonType.CANCEL);
        ListView<Pedido> list = new ListView<>(); list.setId("ordersList");
        configureList(list, pedido -> "Pedido #" + pedido.getNumeroPedido() + " • " + mesaName(pedido.getMesa()) + " • " + pedido.getStatus());
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
        dialog.getDialogPane().setContent(content(label("Pedidos desta sessão", "section-title"), list, details));
        if (activeOrder != null) list.getSelectionModel().select(activeOrder);
        dialog.showAndWait().ifPresent(pedido -> {
            activeOrder = pedido; selectedMesa = pedido.getMesa(); renderOrder(); renderProducts();
            statusLabel.setText("Pedido #" + pedido.getNumeroPedido() + " selecionado para incluir itens.");
        });
    }
}
