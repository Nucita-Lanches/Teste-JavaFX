package br.com.nucitalanches.preview;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.*;

/** Interface independente: dados e pedidos locais, sem persistência. */
public class MenuController {
    @FXML private TextField searchField;
    @FXML private TilePane productGrid;
    @FXML private VBox cartItems;
    @FXML private ToggleButton allCategory, sweetsCategory, drinksCategory, snacksCategory;
    @FXML private Label emptyLabel, countLabel, tableLabel, statusLabel, subtotalLabel, totalLabel;
    @FXML private Button checkoutButton;
    private final ToggleGroup categories = new ToggleGroup();
    private final Map<Product, Integer> cart = new LinkedHashMap<>();
    private final List<String> history = new ArrayList<>();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private Image foods;
    private Image kojima;
    private Image lendaNucita;
    private String category = "Todos";
    private record Product(String name, String description, String category, int cents, int photo, String tag) { }
    private final Product secretProduct = new Product("Hideo Kojima", "Pensando em ideias lendárias. Item raríssimo da Nucita.", "Easter egg", -1, -1, "1.000.000 AURA");
    private final Product lendaProduct = new Product("A Lenda Nucita", "Uma aparição raríssima no cardápio. Só quem sabe, sabe.", "Easter egg", -2, -2, "SEGREDO");
    private final List<Product> products = List.of(
        new Product("Brownie da casa", "Chocolate intenso e creme de avelã.", "Doces", 800, 0, "MAIS PEDIDO"),
        new Product("Milk-shake Nucita", "Chocolate, cremosidade e muita vontade.", "Bebidas", 1400, 1, "FAVORITO"),
        new Product("Cookie recheado", "Crocante por fora. Avelã por dentro.", "Doces", 750, 2, "DA CASA"),
        new Product("Smash da casa", "Pão macio, queijo e carne na chapa.", "Lanches", 2200, 3, "ESPECIAL")
    );

    @FXML private void initialize() {
        foods = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/foods.png")));
        kojima = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/kojima.png")));
        lendaNucita = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/lenda-nucita.jpg")));
        for (ToggleButton button : List.of(allCategory, sweetsCategory, drinksCategory, snacksCategory)) button.setToggleGroup(categories);
        allCategory.setSelected(true);
        searchField.textProperty().addListener((obs, old, value) -> renderProducts());
        // Sugestão inicial para explorar os controles; não é um pedido real.
        cart.put(products.get(0), 1);
        cart.put(products.get(1), 1);
        renderProducts();
        renderCart();
        statusLabel.setText("Sugestão inicial. Ajuste como preferir.");
    }

    private Label label(String text, String style) {
        Label label = new Label(text); label.getStyleClass().add(style); return label;
    }
    private Region spacer() { Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS); return r; }
    private String money(int cents) { return currency.format(cents / 100.0); }
    private String normalize(String text) { return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT); }
    private ImageView photo(Product product, int width, int height) {
        if (product.photo < 0) {
            ImageView portrait = new ImageView(product.photo == -2 ? lendaNucita : kojima); portrait.setFitWidth(width); portrait.setFitHeight(height); portrait.setPreserveRatio(false);
            return portrait;
        }
        ImageView view = new ImageView(foods);
        double w = foods.getWidth() / 2, h = foods.getHeight() / 2;
        // Fit a centered landscape window inside each quadrant, without stretching.
        double cropHeight = w * height / width;
        view.setViewport(new Rectangle2D((product.photo % 2) * w, (product.photo / 2) * h + (h - cropHeight) / 2, w, cropHeight));
        view.setFitWidth(width); view.setFitHeight(height);
        Rectangle clip = new Rectangle(width, height); clip.setArcWidth(15); clip.setArcHeight(15); view.setClip(clip);
        return view;
    }
    private void renderProducts() {
        productGrid.getChildren().clear();
        String query = normalize(searchField.getText());
        List<Product> visible = new ArrayList<>(products);
        if (query.contains("kojima")) visible.add(secretProduct);
        boolean lendaQuery = isLendaSearch(query);
        if (lendaQuery) visible.add(lendaProduct);
        for (Product product : visible) {
            if (product == secretProduct && !query.contains("kojima")) continue;
            if (product == lendaProduct && !lendaQuery) continue;
            if (product != secretProduct && !category.equals("Todos") && !category.equals(product.category)) continue;
            if (product != lendaProduct && !normalize(product.name + " " + product.description).contains(query)) continue;
            VBox card = new VBox(); card.setPrefWidth(188); card.setMaxWidth(188); card.getStyleClass().add("product-card");
            StackPane artwork = new StackPane(photo(product, 170, 124));
            Label tag = label(product.tag, "product-tag"); StackPane.setAlignment(tag, Pos.TOP_LEFT); StackPane.setMargin(tag, new Insets(8)); artwork.getChildren().add(tag);
            Label name = label(product.name, "product-name");
            Label description = label(product.description, "product-description"); description.setWrapText(true);
            Button add = new Button("+"); add.getStyleClass().add("add-button"); add.setAccessibleText("Adicionar " + product.name); add.setOnAction(e -> add(product));
            String displayPrice = product.cents == -1 ? "1.000.000 aura" : product.cents == -2 ? "todas as suas Nucitas" : money(product.cents);
            HBox footer = new HBox(label(displayPrice, "product-price"), spacer(), add); footer.setAlignment(Pos.CENTER_LEFT);
            VBox info = new VBox(name, description, footer); info.getStyleClass().add("product-body");
            card.getChildren().addAll(artwork, info); productGrid.getChildren().add(card);
        }
        boolean empty = productGrid.getChildren().isEmpty(); emptyLabel.setVisible(empty); emptyLabel.setManaged(empty);
    }
    private void add(Product p) { cart.merge(p, 1, Integer::sum); statusLabel.setText(p.name + " adicionado."); renderCart(); }
    private void remove(Product p) { cart.computeIfPresent(p, (key, value) -> value == 1 ? null : value - 1); renderCart(); }
    private void renderCart() {
        cartItems.getChildren().clear(); int cents = 0, count = 0;
        for (var entry : cart.entrySet()) {
            Product p = entry.getKey(); int quantity = entry.getValue(); if (p.cents > 0) cents += p.cents * quantity; count += quantity;
            Button minus = new Button("−"); minus.getStyleClass().add("quantity"); minus.setAccessibleText("Remover uma unidade de " + p.name); minus.setOnAction(e -> remove(p));
            Button plus = new Button("+"); plus.getStyleClass().add("quantity"); plus.setOnAction(e -> add(p));
            HBox controls = new HBox(6, minus, label(Integer.toString(quantity), "cart-price"), plus); controls.setAlignment(Pos.CENTER_LEFT);
            VBox text = new VBox(6, label(p.name, "cart-item-name"), controls); HBox.setHgrow(text, Priority.ALWAYS);
            HBox row = new HBox(9, photo(p, 44, 44), text); row.setAlignment(Pos.CENTER_LEFT);
            String itemTotal = p.cents == -1 ? "1.000.000 aura" : p.cents == -2 ? "todas as suas Nucitas" : money(p.cents * quantity);
            VBox item = new VBox(8, row, label(itemTotal, "cart-price")); cartItems.getChildren().add(item);
        }
        if (count == 0) { Label empty = label("Seu pedido começa com uma boa escolha. Adicione um favorito!", "muted-label"); empty.setWrapText(true); cartItems.getChildren().add(empty); }
        countLabel.setText(Integer.toString(count)); subtotalLabel.setText(money(cents)); totalLabel.setText(money(cents)); checkoutButton.setDisable(count == 0);
    }
    private void filter(String value, ToggleButton button) { category = value; button.setSelected(true); renderProducts(); }
    private boolean isLendaSearch(String query) {
        return query.contains("gordo") && query.contains("nucita") && query.contains("cardoso") && query.contains("custodio");
    }
    @FXML private void filterAll() { filter("Todos", allCategory); }
    @FXML private void filterSweets() { filter("Doces", sweetsCategory); }
    @FXML private void filterDrinks() { filter("Bebidas", drinksCategory); }
    @FXML private void filterSnacks() { filter("Lanches", snacksCategory); }
    @FXML private void showMenu() { searchField.clear(); filterAll(); }
    @FXML private void showFavorites() { searchField.clear(); filterSweets(); }
    @FXML private void showTables() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(tableLabel.getText(), List.of("Mesa 01", "Mesa 02", "Mesa 03", "Mesa 04", "Mesa 05", "Mesa 06"));
        dialog.setTitle("Nucita • Mesas"); dialog.setHeaderText("Onde vamos servir seu pedido?"); dialog.setContentText("Escolha uma mesa:");
        dialog.showAndWait().ifPresent(tableLabel::setText);
    }
    @FXML private void showOrders() { showMessage("Pedidos desta prévia", history.isEmpty() ? "Você ainda não confirmou nenhum pedido nesta sessão." : String.join("\n\n", history)); }
    @FXML private void checkout() {
        if (cart.isEmpty()) return;
        String receipt = tableLabel.getText() + " • " + countLabel.getText() + " itens • " + totalLabel.getText();
        history.add(receipt); cart.clear(); renderCart(); statusLabel.setText("Pedido registrado nesta prévia.");
        showMessage("Tudo certo!", receipt + "\n\nDemonstração local. Nenhum pedido foi enviado ao restaurante.");
    }
    private void showMessage(String title, String text) { Alert dialog = new Alert(Alert.AlertType.INFORMATION); dialog.setTitle("Nucita Lanches"); dialog.setHeaderText(title); dialog.setContentText(text); dialog.showAndWait(); }
}
