package br.com.nucitalanches.preview;

import com.senai.nucitalanches.nucitalanches.domain.*;
import com.senai.nucitalanches.nucitalanches.storage.BancoDeDados;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/** Exercita os botões reais do FXML e confere as alterações no domínio. Requer ambiente gráfico. */
class MenuControllerIntegrationTest {
    private Parent root;
    private Stage stage;

    @Test
    void conectaCadastrosMesasEPedidosPelaInterface() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> { verifyFlow(); return null; });
        Platform.startup(() -> Platform.setImplicitExit(false));
        Platform.runLater(task);
        try { task.get(40, TimeUnit.SECONDS); }
        finally { Platform.exit(); }
    }

    private void verifyFlow() throws Exception {
        root = FXMLLoader.load(NucitaPreviewApp.class.getResource("/view/exemplo-cardapio.fxml"));
        stage = new Stage();
        stage.setScene(new Scene(root, 1440, 900));
        stage.show();
        BancoDeDados banco = BancoDeDados.getInstancia();
        int productsBefore = banco.listarProdutos().size();
        int tablesBefore = banco.listarMesas().size();
        int ordersBefore = banco.listarPedidos().size();
        assertTrue(button(root, "newOrderButton").isDisabled());
        modal("productsNav", "productsDialog", pane -> {
            text(pane, "productPrice").setText("12,50");
            button(pane, "saveProduct").fire();
            assertEquals(productsBefore, banco.listarProdutos().size(), "Nome vazio deve ser recusado");
            text(pane, "productName").setText("  Pão de queijo UI  ");
            for (String invalid : List.of("abc", "-2", "1.000", "NaN", "1,2,3")) {
                text(pane, "productPrice").setText(invalid);
                button(pane, "saveProduct").fire();
                assertEquals(productsBefore, banco.listarProdutos().size());
            }
            text(pane, "productPrice").setText("12,50");
            button(pane, "saveProduct").fire();
            assertEquals(productsBefore + 1, banco.listarProdutos().size());
            assertEquals(productsBefore + 1, list(pane, "productsList").getItems().size());
            snapshot(pane, "produtos");
        });
        Produto product = banco.listarProdutos().stream().filter(p -> p.getNome().equals("Pão de queijo UI")).findFirst().orElseThrow();
        assertEquals(12.5, product.getPreco());
        assertTrue(button(root, "addProduct-" + product.getId()).isDisabled());
        text(root, "searchField").setText("pao");
        assertNotNull(root.lookup("#addProduct-" + product.getId()), "Busca deve ignorar acentos");
        text(root, "searchField").setText("produto inexistente xyz");
        assertTrue(((Label) root.lookup("#emptyLabel")).isVisible());
        button(root, "menuNav").fire();

        modal("tablesNav", "tablesDialog", pane -> {
            for (String invalid : List.of("0", "-1", "abc", "999999999999")) {
                text(pane, "tableNumber").setText(invalid); button(pane, "saveTable").fire();
                assertEquals(tablesBefore, banco.listarMesas().size());
            }
            text(pane, "tableNumber").setText("40401"); button(pane, "saveTable").fire();
            assertEquals(tablesBefore + 1, banco.listarMesas().size());
            text(pane, "tableNumber").setText("40401"); button(pane, "saveTable").fire();
            assertEquals(tablesBefore + 1, banco.listarMesas().size(), "Mesa duplicada deve ser recusada");
            text(pane, "tableNumber").setText("40402"); button(pane, "saveTable").fire();
            text(pane, "tableNumber").setText("40401"); button(pane, "findTable").fire();
            assertSame(banco.buscarMesaPorNumero(40401), list(pane, "tablesList").getSelectionModel().getSelectedItem());
            text(pane, "tableNumber").setText("40403"); button(pane, "findTable").fire();
            assertEquals("Mesa não encontrada.", ((Label) pane.lookup("#tableFeedback")).getText());
            snapshot(pane, "mesas");
            button(pane, "selectTable").fire();
        });
        assertFalse(button(root, "newOrderButton").isDisabled());
        button(root, "newOrderButton").fire();
        Pedido first = banco.listarPedidos().stream().reduce((a, b) -> b).orElseThrow();
        assertEquals(40401, first.getMesa().getNumero());
        assertEquals(StatusPedido.ABERTO, first.getStatus());
        modal("addProduct-" + product.getId(), "addItemDialog", pane -> {
            for (String invalid : List.of("0", "-3", "abc", "1,5", "2147483648")) {
                text(pane, "itemQuantity").setText(invalid); button(pane, "saveItem").fire();
                assertTrue(first.getItens().isEmpty()); assertTrue(pane.getScene().getWindow().isShowing());
            }
            text(pane, "itemQuantity").setText("2"); button(pane, "saveItem").fire();
        });
        assertEquals(2, first.getItens().get(0).getQuantidade());
        assertSame(product, first.getItens().get(0).getProduto());
        assertEquals("2", ((Label) root.lookup("#countLabel")).getText());
        modal("tablesNav", "tablesDialog", pane -> {
            text(pane, "tableNumber").setText("40402"); button(pane, "findTable").fire(); button(pane, "selectTable").fire();
        });
        assertEquals("Mesa 40401", ((Label) root.lookup("#tableLabel")).getText(), "Escolher outra mesa não deve mover o pedido atual");
        button(root, "newOrderButton").fire();
        Pedido second = banco.listarPedidos().stream().reduce((a, b) -> b).orElseThrow();
        assertEquals(40402, second.getMesa().getNumero());
        assertTrue(second.getItens().isEmpty());
        modal("ordersNav", "ordersDialog", pane -> {
            @SuppressWarnings("unchecked") ListView<Pedido> list = (ListView<Pedido>) (ListView<?>) list(pane, "ordersList");
            list.getSelectionModel().select(first);
            assertTrue(((TextArea) pane.lookup("#orderDetails")).getText().contains("2 × Pão de queijo UI"));
            snapshot(pane, "pedidos"); button(pane, "selectOrder").fire();
        });
        modal("addProduct-" + product.getId(), "addItemDialog", pane -> button(pane, "saveItem").fire());
        assertEquals(2, first.getItens().size());
        assertEquals("3", ((Label) root.lookup("#countLabel")).getText());
        assertTrue(second.getItens().isEmpty(), "Continuar pedido deve incluir no pedido selecionado");
        assertEquals(ordersBefore + 2, banco.listarPedidos().size());
        button(root, "newOrderButton").fire();
        Pedido third = banco.listarPedidos().stream().reduce((a, b) -> b).orElseThrow();
        assertSame(first.getMesa(), third.getMesa(), "Uma mesa pode ter vários pedidos");
        modal("ordersNav", "ordersDialog", pane -> {
            @SuppressWarnings("unchecked") ListView<Pedido> list = (ListView<Pedido>) (ListView<?>) list(pane, "ordersList");
            list.getSelectionModel().select(first); button(pane, "selectOrder").fire();
        });
        snapshot(root, "atendimento"); stage.close();
    }

    private Button button(Node owner, String id) { return (Button) owner.lookup("#" + id); }
    private TextField text(Node owner, String id) { return (TextField) owner.lookup("#" + id); }
    private ListView<?> list(Node owner, String id) { return (ListView<?>) owner.lookup("#" + id); }

    private void modal(String opener, String paneId, Consumer<DialogPane> verify) {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                DialogPane pane = null;
                for (Window window : new ArrayList<>(Window.getWindows())) {
                    if (window.isShowing() && window != stage && window.getScene().getRoot().lookup("#" + paneId) instanceof DialogPane found) pane = found;
                }
                assertNotNull(pane, "Diálogo deve abrir: " + paneId);
                verify.accept(pane);
            } catch (Throwable exception) { failure.set(exception); }
            finally {
                for (Window window : new ArrayList<>(Window.getWindows())) if (window != stage && window.isShowing()) window.hide();
            }
        });
        button(root, opener).fire();
        if (failure.get() != null) throw new AssertionError("Falha no diálogo " + paneId, failure.get());
    }

    private void snapshot(Node node, String name) {
        try {
            node.applyCss();
            if (node instanceof Parent parent) parent.layout();
            WritableImage image = node.snapshot(null, null);
            BufferedImage output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < output.getHeight(); y++) for (int x = 0; x < output.getWidth(); x++) output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            Path directory = Path.of("target", "ui-check"); Files.createDirectories(directory);
            ImageIO.write(output, "png", directory.resolve(name + ".png").toFile());
        } catch (Exception exception) { throw new AssertionError(exception); }
    }
}
