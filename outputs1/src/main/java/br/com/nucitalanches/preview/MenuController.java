package br.com.nucitalanches.preview;

import javafx.fxml.FXML;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.Node;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.*;

/** Interface independente: dados e pedidos locais, sem persistência. */
public class MenuController {
    @FXML private BorderPane appRoot;
    @FXML private VBox orderPanel;
    @FXML private Button menuNav, productNav, registerNav, usersNav;
    @FXML private ToggleButton themeToggle, motionToggle;
    @FXML private Label pageTitle, pageEyebrow;
    @FXML private Label profileName;
    private Node sidebar;
    private User sessionUser;
    private final PasswordStore passwords = new PasswordStore();
    private boolean darkTheme = true;
    private ParallelTransition pageAnimation;
    private Node animatedPage;
    private final Map<Node, Animation> entrances = new HashMap<>();
    private static final Interpolator SOFT = Interpolator.SPLINE(0.22, 0.61, 0.36, 1);
    @FXML private StackPane sidebarLogoSlot, heroLogoSlot;
    @FXML private TextField searchField;
    @FXML private TilePane productGrid;
    @FXML private BorderPane pageContainer;
    @FXML private ScrollPane menuView;
    @FXML private VBox cartItems;
    @FXML private ToggleButton allCategory, sweetsCategory, drinksCategory, snacksCategory;
    @FXML private Label emptyLabel, countLabel, tableLabel, statusLabel, subtotalLabel, totalLabel;
    @FXML private Button checkoutButton;
    private final ToggleGroup categories = new ToggleGroup();
    private final Map<Product, Integer> cart = new LinkedHashMap<>();
    private final List<String> history = new ArrayList<>();
    private record User(long id, String name, String email, String phone) { }
    private final List<User> users = new ArrayList<>();
    private long nextUserId = 1;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private Image foods;
    private Image kojima;
    private Image lendaNucita;
    private String category = "Todos";
    private record Product(String name, String description, String category, int cents, int photo, String tag) { }
    private final Product secretProduct = new Product("Hideo Kojima", "Pensando em ideias lendárias. Item raríssimo da Nucita.", "Easter egg", -1, -1, "1.000.000 AURA");
    private final Product lendaProduct = new Product("A Lenda Nucita", "Uma aparição raríssima no cardápio. Só quem sabe, sabe.", "Easter egg", -2, -2, "SEGREDO");
    private final List<Product> products = new ArrayList<>(List.of(
        new Product("Brownie da casa", "Chocolate intenso e creme de avelã.", "Doces", 800, 0, "MAIS PEDIDO"),
        new Product("Milk-shake Nucita", "Chocolate, cremosidade e muita vontade.", "Bebidas", 1400, 1, "FAVORITO"),
        new Product("Cookie recheado", "Crocante por fora. Avelã por dentro.", "Doces", 750, 2, "DA CASA"),
        new Product("Smash da casa", "Pão macio, queijo e carne na chapa.", "Lanches", 2200, 3, "ESPECIAL")
    ));

    @FXML private void initialize() {
        appRoot.getStyleClass().add("dark");
        themeToggle.setSelected(true);
        themeToggle.setText("Tema escuro");
        motionToggle.setSelected(true);
        sidebarLogoSlot.getChildren().add(brandImage(138));
        heroLogoSlot.getChildren().add(brandImage(184));
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
        sidebar = appRoot.getLeft();
        showLogin("");
    }

    private Label label(String text, String style) {
        Label label = new Label(text); label.getStyleClass().add(style); return label;
    }
    private Region spacer() { Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS); return r; }
    private String money(int cents) { return currency.format(cents / 100.0); }
    private String normalize(String text) { return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT); }
    private StackPane photo(Product product, int width, int height) {
        Image image = product.photo == -2 ? lendaNucita : product.photo == -1 ? kojima : foods;
        double sourceW = image.getWidth(), sourceH = image.getHeight(), x = 0, y = 0;
        if (product.photo >= 0) {
            sourceW /= 2; sourceH /= 2;
            x = (product.photo % 2) * sourceW; y = (product.photo / 2) * sourceH;
        }
        // Center-cover inside the selected image/quadrant; never stretch a portrait.
        double ratio = (double) width / height;
        double cropW = Math.min(sourceW, sourceH * ratio), cropH = cropW / ratio;
        ImageView view = new ImageView(image);
        view.setViewport(new Rectangle2D(x + (sourceW - cropW) / 2, y + (sourceH - cropH) / 2, cropW, cropH));
        view.setFitWidth(width); view.setFitHeight(height); view.setSmooth(true);
        StackPane frame = new StackPane(view);
        frame.setMinSize(width, height); frame.setPrefSize(width, height); frame.setMaxSize(width, height);
        Rectangle clip = new Rectangle(width, height); clip.setArcWidth(20); clip.setArcHeight(20); frame.setClip(clip);
        frame.getProperties().put("photo", view);
        return frame;
    }
    private StackPane brandImage(int size) {
        ImageView logo = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"))));
        logo.setFitWidth(size); logo.setFitHeight(size); logo.setPreserveRatio(true); logo.setSmooth(true);
        StackPane frame = new StackPane(logo);
        frame.setMinSize(size + 18, size + 18); frame.setMaxSize(size + 18, size + 18);
        frame.getStyleClass().add("brand-frame");
        Rectangle clip = new Rectangle(size, size); clip.setArcWidth(28); clip.setArcHeight(28); logo.setClip(clip);
        return frame;
    }
    private void renderProducts() {
        for (Node card : productGrid.getChildren()) {
            Animation entry = entrances.remove(card); if (entry != null) entry.stop();
            Animation hover = (Animation) card.getProperties().get("hoverAnimation"); if (hover != null) hover.stop();
        }
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
            StackPane photoFrame = photo(product, 170, 136);
            StackPane artwork = new StackPane(photoFrame);
            Label tag = label(product.tag, "product-tag"); StackPane.setAlignment(tag, Pos.TOP_LEFT); StackPane.setMargin(tag, new Insets(8)); artwork.getChildren().add(tag);
            Label name = label(product.name, "product-name");
            Label description = label(product.description, "product-description"); description.setWrapText(true);
            Button add = new Button("+"); add.getStyleClass().add("add-button"); add.setAccessibleText("Adicionar " + product.name); add.setOnAction(e -> add(product));
            String displayPrice = product.cents == -1 ? "1.000.000 aura" : product.cents == -2 ? "todas as suas Nucitas" : money(product.cents);
            HBox footer = new HBox(label(displayPrice, "product-price"), spacer(), add); footer.setAlignment(Pos.CENTER_LEFT);
            VBox info = new VBox(name, description, footer); info.getStyleClass().add("product-body");
            name.setWrapText(true);
            card.getChildren().addAll(artwork, info); productGrid.getChildren().add(card);
            installCardMotion(card, (ImageView) photoFrame.getProperties().get("photo"));
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
    private void filter(String value, ToggleButton button) { category = value; button.setSelected(true); navigate(menuView, menuNav, "Hoje pede algo especial.", false); renderProducts(); int index = 0; for (Node card : productGrid.getChildren()) revealCard(card, Math.min(index++, 6) * 35); }
    private boolean isLendaSearch(String query) {
        return query.contains("gordo") && query.contains("nucita") && query.contains("cardoso") && query.contains("custodio");
    }
    @FXML private void filterAll() { filter("Todos", allCategory); }
    @FXML private void filterSweets() { filter("Doces", sweetsCategory); }
    @FXML private void filterDrinks() { filter("Bebidas", drinksCategory); }
    @FXML private void filterSnacks() { filter("Lanches", snacksCategory); }
    @FXML private void showMenu() { searchField.clear(); filterAll(); }
    @FXML private void showFavorites() { searchField.clear(); filterSweets(); }
    @FXML private void showProductRegistration() {
        navigate(createProductRegistrationView(), productNav, "Gestão do cardápio", true);
    }
    private Node createProductRegistrationView() {
        Label eyebrow = label("CARDÁPIO • ADMINISTRAÇÃO", "eyebrow");
        Label title = label("Cadastrar produto", "page-title");
        Label subtitle = label("Adicione uma opção para ela aparecer no cardápio desta prévia.", "muted-label");
        VBox heading = new VBox(7, eyebrow, title, subtitle);

        TextField nameField = new TextField();
        nameField.setPromptText("Ex.: Brownie com Nucita");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Conte um pouco sobre os ingredientes e o sabor");
        descriptionField.setWrapText(true);
        descriptionField.setPrefRowCount(3);
        ComboBox<String> categoryField = new ComboBox<>();
        categoryField.getItems().addAll("Doces", "Bebidas", "Lanches");
        categoryField.setPromptText("Selecione uma categoria");
        categoryField.setMaxWidth(Double.MAX_VALUE);
        TextField priceField = new TextField();
        priceField.setPromptText("0,00");

        VBox form = new VBox(9,
            label("Nome do produto", "form-label"), nameField,
            label("Descrição", "form-label"), descriptionField,
            label("Categoria", "form-label"), categoryField,
            label("Preço (R$)", "form-label"), priceField
        );
        form.getStyleClass().add("registration-card");

        Label feedback = label(" ", "form-feedback");
        Button cancel = new Button("Voltar ao cardápio");
        cancel.getStyleClass().add("text-button");
        cancel.setOnAction(event -> showMenu());
        Button save = new Button("Cadastrar produto   →");
        save.getStyleClass().add("checkout-button");
        save.setOnAction(event -> {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String categoryValue = categoryField.getValue();
            String priceText = priceField.getText().trim().replace("R$", "").replace(" ", "").replace(',', '.');
            if (name.isEmpty() || description.isEmpty() || categoryValue == null || priceText.isEmpty()) {
                feedback.setText("Preencha todos os campos para continuar.");
                return;
            }
            int cents;
            try {
                double parsed = Double.parseDouble(priceText);
                if (!Double.isFinite(parsed) || parsed <= 0 || parsed > 999999.99) throw new NumberFormatException();
                cents = (int) Math.round(parsed * 100);
            } catch (NumberFormatException exception) {
                feedback.setText("Informe um preço válido, maior que zero.");
                return;
            }
            products.add(new Product(name, description, categoryValue, cents, products.size() % 4, "NOVO"));
            searchField.clear();
            filterAll();
            statusLabel.setText(name + " cadastrado nesta sessão.");
        });
        HBox actions = new HBox(14, cancel, save);
        actions.setAlignment(Pos.CENTER_RIGHT);
        VBox content = new VBox(22, heading, form, feedback, actions);
        content.getStyleClass().add("registration-content");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("menu-scroll");
        return scroll;
    }
    @FXML private void showUserRegistration() {
        if (sessionUser == null && !users.isEmpty()) { showLogin(""); return; }
        TextField name = new TextField();
        name.setPromptText("Nome completo");
        TextField email = new TextField();
        email.setPromptText("nome@exemplo.com");
        TextField phone = new TextField();
        phone.setPromptText("(47) 99999-9999");
        PasswordField password = new PasswordField();
        password.setPromptText("Pelo menos 8 caracteres");
        PasswordField confirmation = new PasswordField();
        confirmation.setPromptText("Repita a senha");
        VBox form = new VBox(9,
            label("Nome completo *", "form-label"), name,
            label("E-mail *", "form-label"), email,
            label("Telefone (opcional)", "form-label"), phone,
            label("Senha *", "form-label"), password,
            label("Confirmar senha *", "form-label"), confirmation);
        form.getStyleClass().add("registration-card");
        Label feedback = label("* Campos obrigatórios", "form-feedback");
        feedback.setWrapText(true);
        Button cancel = new Button(sessionUser == null ? "Voltar ao login" : "Consultar usuários");
        cancel.getStyleClass().add("text-button");
        cancel.setOnAction(event -> { if (sessionUser == null) showLogin(""); else showUserConsultation(); });
        Button save = new Button("Cadastrar usuário   →");
        save.getStyleClass().add("checkout-button");
        save.setOnAction(event -> {
            String userName = name.getText().strip();
            String userEmail = email.getText().strip().toLowerCase(Locale.ROOT);
            String userPhone = phone.getText().strip();
            if (userName.isEmpty() || userEmail.isEmpty()) {
                feedback.setText("Preencha o nome e o e-mail.");
                (userName.isEmpty() ? name : email).requestFocus();
                return;
            }
            if (!userEmail.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
                feedback.setText("Informe um e-mail válido, como nome@exemplo.com.");
                email.requestFocus();
                return;
            }
            String digits = userPhone.replaceAll("\\D", "");
            if (!userPhone.isEmpty() && (!userPhone.matches("[+()\\d\\s.-]+") || digits.length() < 10 || digits.length() > 13)) {
                feedback.setText("Informe um telefone com DDD ou deixe o campo vazio.");
                phone.requestFocus();
                return;
            }
            if (users.stream().anyMatch(user -> user.email().equalsIgnoreCase(userEmail))) {
                feedback.setText("Este e-mail já está cadastrado. Consulte a lista de usuários.");
                email.requestFocus();
                return;
            }
            if (password.getText().length() < 8 || password.getText().length() > 128) {
                feedback.setText("A senha deve ter de 8 a 128 caracteres.");
                password.requestFocus();
                return;
            }
            if (!password.getText().equals(confirmation.getText())) {
                feedback.setText("As senhas não coincidem.");
                confirmation.requestFocus();
                return;
            }
            char[] secret = password.getText().toCharArray();
            try { passwords.register(userEmail, secret); }
            finally { Arrays.fill(secret, '\0'); password.clear(); confirmation.clear(); }
            users.add(new User(nextUserId++, userName, userEmail, userPhone));
            if (sessionUser == null) { showLogin("Conta criada. Entre com seu e-mail e senha."); return; }
            showUserConsultation();
            statusLabel.setText(userName + " cadastrado com sucesso nesta sessão.");
        });
        HBox actions = new HBox(14, cancel, save);
        actions.setAlignment(Pos.CENTER_RIGHT);
        displayUserPage("Cadastrar usuário", "Preencha os dados de contato do usuário.", form, feedback, actions);
    }

    @FXML private void showUserConsultation() {
        TextField query = new TextField();
        query.setPromptText("Buscar por nome, e-mail ou telefone");
        query.setAccessibleText("Buscar usuários por nome, e-mail ou telefone");
        query.getStyleClass().add("user-search");
        VBox results = new VBox(12);
        Label count = label("", "muted-label");
        Runnable refresh = () -> {
            results.getChildren().clear();
            String term = normalize(query.getText().strip());
            List<User> visible = users.stream().filter(user -> {
                String contacts = normalize(user.name() + " " + user.email() + " " + user.phone());
                String digits = term.replaceAll("\\D", "");
                boolean phoneMatch = term.matches("[+()\\d\\s.-]+") && !digits.isEmpty()
                    && user.phone().replaceAll("\\D", "").contains(digits);
                return contacts.contains(term) || phoneMatch;
            }).toList();
            count.setText(visible.size() + " de " + users.size() + " usuários");
            if (visible.isEmpty()) {
                Label empty = label(users.isEmpty()
                    ? "Nenhum usuário cadastrado. Use Novo usuário para começar."
                    : "Nenhum usuário encontrado. Tente outro nome, e-mail ou telefone.", "muted-label");
                empty.setWrapText(true);
                results.getChildren().add(empty);
            }
            for (User user : visible) {
                Label userName = label(user.name(), "section-title");
                userName.setWrapText(true);
                Label contact = label(user.email(), "user-contact");
                contact.setWrapText(true);
                VBox row = new VBox(7, label("USUÁRIO #" + user.id(), "tiny-caption"), userName, contact,
                    label(user.phone().isEmpty() ? "Telefone não informado" : user.phone(), "muted-label"));
                row.getStyleClass().add("registration-card");
                results.getChildren().add(row);
            }
        };
        query.textProperty().addListener((observable, before, after) -> refresh.run());
        refresh.run();
        Button create = new Button("＋   Novo usuário");
        create.getStyleClass().add("checkout-button");
        create.setOnAction(event -> showUserRegistration());
        displayUserPage("Consultar usuários", "Encontre os usuários cadastrados e consulte seus dados de contato.", create, query, count, results);
    }

    private void displayUserPage(String title, String subtitle, Node... children) {
        Label description = label(subtitle, "muted-label");
        description.setWrapText(true);
        VBox heading = new VBox(7, label("USUÁRIOS • ADMINISTRAÇÃO", "eyebrow"), label(title, "page-title"), description);
        VBox content = new VBox(22, heading);
        content.getChildren().addAll(children);
        content.getChildren().add(label("Cadastros disponíveis somente durante esta sessão.", "muted-label"));
        content.getStyleClass().add("registration-content");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("menu-scroll");
        if (sessionUser == null && users.isEmpty()) appRoot.setCenter(scroll);
        else navigate(scroll, title.equals("Cadastrar usuário") ? registerNav : usersNav, "Gestão de usuários", true);
    }

    private void showLogin(String message) {
        if (pageAnimation != null) pageAnimation.stop();
        appRoot.setLeft(null);
        appRoot.setRight(null);
        StackPane brand = new StackPane(brandImage(100));
        Label title = label("Bem-vindo de volta", "page-title");
        Label intro = label("Entre para acessar o sistema Nucita Lanches.", "muted-label");
        intro.setWrapText(true);
        TextField email = new TextField();
        email.setPromptText("nome@exemplo.com");
        PasswordField password = new PasswordField();
        password.setPromptText("Sua senha");
        Label emailLabel = label("E-mail", "form-label"); emailLabel.setLabelFor(email);
        Label passwordLabel = label("Senha", "form-label"); passwordLabel.setLabelFor(password);
        Label feedback = label(message, "form-feedback");
        feedback.setWrapText(true);
        feedback.setMinHeight(34);
        Button enter = new Button("Entrar no sistema   →");
        enter.getStyleClass().add("checkout-button");
        enter.setMaxWidth(Double.MAX_VALUE);
        enter.setDefaultButton(true);
        enter.setOnAction(event -> {
            String address = email.getText().strip().toLowerCase(Locale.ROOT);
            if (address.isEmpty() || password.getText().isEmpty()) {
                feedback.setText("Informe seu e-mail e sua senha.");
                return;
            }
            if (password.getText().length() > 128) {
                feedback.setText("E-mail ou senha incorretos."); password.clear(); return;
            }
            char[] secret = password.getText().toCharArray();
            boolean valid;
            try { valid = passwords.matches(address, secret); }
            finally { Arrays.fill(secret, '\0'); password.clear(); }
            if (!valid) {
                feedback.setText("E-mail ou senha incorretos.");
                password.requestFocus();
                return;
            }
            sessionUser = users.stream().filter(user -> user.email().equals(address)).findFirst().orElseThrow();
            profileName.setText(sessionUser.name());
            appRoot.setLeft(sidebar);
            appRoot.setCenter(pageContainer);
            showMenu();
            statusLabel.setText("Bem-vindo, " + sessionUser.name() + ".");
        });
        VBox card = new VBox(12, brand, label("NUCITA LANCHES • ACESSO", "eyebrow"), title, intro,
            emailLabel, email, passwordLabel, password, feedback, enter);
        card.getStyleClass().addAll("registration-card", "login-card");
        card.setMaxWidth(460);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        if (users.isEmpty()) {
            Button create = new Button("Criar primeiro acesso");
            create.getStyleClass().add("text-button");
            create.setMaxWidth(Double.MAX_VALUE);
            create.setOnAction(event -> showUserRegistration());
            card.getChildren().add(create);
        }
        Label local = label("Prévia local • contas disponíveis até fechar o aplicativo.", "muted-label");
        local.setWrapText(true);
        card.getChildren().add(local);
        StackPane stage = new StackPane(card);
        stage.setPadding(new Insets(30));
        ScrollPane scroll = new ScrollPane(stage);
        scroll.setFitToWidth(true); scroll.setFitToHeight(true);
        scroll.getStyleClass().add("menu-scroll");
        appRoot.setCenter(scroll);
        revealPage(card);
    }

    @FXML private void logout() {
        sessionUser = null;
        cart.clear(); history.clear(); renderCart();
        showLogin("Você saiu do sistema.");
    }

    private void navigate(Node view, Button active, String title, boolean administration) {
        if (sessionUser == null) { showLogin(""); return; }
        if (pageAnimation != null) pageAnimation.stop();
        Node previous = pageContainer.getCenter();
        if (previous != null) { previous.setOpacity(1); previous.setTranslateY(0); }
        pageContainer.setCenter(view);
        appRoot.setRight(administration ? null : orderPanel);
        pageTitle.setText(title);
        pageEyebrow.setText(administration ? "NUCITA • ADMINISTRAÇÃO" : "BEM-VINDO À NUCITA");
        for (Button button : List.of(menuNav, productNav, registerNav, usersNav)) {
            button.getStyleClass().remove("nav-active");
            if (button == active) button.getStyleClass().add("nav-active");
        }
        if (view != previous) revealPage(view);
        else { view.setOpacity(1); view.setTranslateY(0); }
    }

    private void revealPage(Node view) {
        if (pageAnimation != null) pageAnimation.stop();
        if (animatedPage != null) { animatedPage.setOpacity(1); animatedPage.setTranslateY(0); }
        animatedPage = view;
        if (!motionToggle.isSelected()) return;
        FadeTransition fade = new FadeTransition(Duration.millis(280), view);
        fade.setFromValue(0); fade.setToValue(1); fade.setInterpolator(SOFT);
        TranslateTransition slide = new TranslateTransition(Duration.millis(340), view);
        slide.setFromY(14); slide.setToY(0); slide.setInterpolator(SOFT);
        pageAnimation = new ParallelTransition(fade, slide); pageAnimation.play();
    }

    private void revealCard(Node card, int delay) {
        if (!motionToggle.isSelected()) return;
        FadeTransition fade = new FadeTransition(Duration.millis(300), card);
        fade.setFromValue(0); fade.setToValue(1); fade.setDelay(Duration.millis(delay)); fade.setInterpolator(SOFT);
        card.setOpacity(0); entrances.put(card, fade);
        fade.setOnFinished(event -> entrances.remove(card)); fade.play();
    }

    private void installCardMotion(Node card, ImageView image) {
        TranslateTransition lift = new TranslateTransition(Duration.millis(260), card);
        lift.setInterpolator(SOFT);
        ScaleTransition zoom = new ScaleTransition(Duration.millis(360), image);
        zoom.setInterpolator(SOFT);
        ParallelTransition hover = new ParallelTransition(lift, zoom);
        card.setOnMouseEntered(event -> {
            hover.stop();
            if (motionToggle.isSelected()) { lift.setToY(-4); zoom.setToX(1.045); zoom.setToY(1.045); hover.play(); }
        });
        card.setOnMouseExited(event -> {
            hover.stop();
            if (!motionToggle.isSelected()) { card.setTranslateY(0); image.setScaleX(1); image.setScaleY(1); return; }
            lift.setToY(0); zoom.setToX(1); zoom.setToY(1); hover.play();
        });
        card.getProperties().put("hoverAnimation", hover);
        card.getProperties().put("photo", image);
    }

    @FXML private void toggleTheme() {
        darkTheme = themeToggle.isSelected();
        appRoot.getStyleClass().remove("dark");
        if (darkTheme) appRoot.getStyleClass().add("dark");
        themeToggle.setText(darkTheme ? "Tema escuro" : "Tema claro");
    }

    @FXML private void toggleMotion() {
        if (!motionToggle.isSelected()) {
            entrances.forEach((node, animation) -> { animation.stop(); node.setOpacity(1); }); entrances.clear();
        }
        if (!motionToggle.isSelected()) for (Node card : productGrid.getChildren()) {
            Animation hover = (Animation) card.getProperties().get("hoverAnimation");
            if (hover != null) hover.stop();
            card.setTranslateY(0);
            ImageView image = (ImageView) card.getProperties().get("photo");
            if (image != null) { image.setScaleX(1); image.setScaleY(1); }
        }
        if (!motionToggle.isSelected() && pageAnimation != null) {
            pageAnimation.stop();
            if (animatedPage != null) { animatedPage.setOpacity(1); animatedPage.setTranslateY(0); }
        }
    }

    private void styleDialog(Dialog<?> dialog) {
        dialog.initOwner(appRoot.getScene().getWindow());
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/css/javafx-nucita-theme.css").toExternalForm());
        if (darkTheme) pane.getStyleClass().add("dark");
    }

    @FXML private void showTables() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(tableLabel.getText(), List.of("Mesa 01", "Mesa 02", "Mesa 03", "Mesa 04", "Mesa 05", "Mesa 06"));
        dialog.setTitle("Nucita • Mesas"); dialog.setHeaderText("Onde vamos servir seu pedido?"); dialog.setContentText("Escolha uma mesa:");
        styleDialog(dialog);
        dialog.showAndWait().ifPresent(tableLabel::setText);
    }
    @FXML private void showOrders() { showMessage("Pedidos desta prévia", history.isEmpty() ? "Você ainda não confirmou nenhum pedido nesta sessão." : String.join("\n\n", history)); }
    @FXML private void checkout() {
        if (cart.isEmpty()) return;
        String receipt = tableLabel.getText() + " • " + countLabel.getText() + " itens • " + totalLabel.getText();
        history.add(receipt); cart.clear(); renderCart(); statusLabel.setText("Pedido registrado nesta prévia.");
        showMessage("Tudo certo!", receipt + "\n\nDemonstração local. Nenhum pedido foi enviado ao restaurante.");
    }
    private void showMessage(String title, String text) { Alert dialog = new Alert(Alert.AlertType.INFORMATION); dialog.setTitle("Nucita Lanches"); dialog.setHeaderText(title); dialog.setContentText(text); styleDialog(dialog); dialog.showAndWait(); }
}
