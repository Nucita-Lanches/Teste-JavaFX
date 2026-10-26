package br.com.nucitalanches.preview;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/** Ícones vetoriais locais, com o mesmo traço em toda a interface. */
final class UiIcons {
    private UiIcons() { }

    static StackPane icon(String name) {
        SVGPath path = new SVGPath();
        path.setContent(switch (name) {
            case "menu" -> "M 2 2 L 8 2 L 8 8 L 2 8 Z M 12 2 L 18 2 L 18 8 L 12 8 Z M 2 12 L 8 12 L 8 18 L 2 18 Z M 12 12 L 18 12 L 18 18 L 12 18 Z";
            case "products" -> "M 3 6 L 17 6 L 19 19 L 1 19 Z M 6 6 L 6 4 A 4 4 0 0 1 14 4 L 14 6";
            case "tables" -> "M 2 3 L 18 3 L 18 13 L 2 13 Z M 5 13 L 5 19 M 15 13 L 15 19 M 6 6 L 14 6";
            case "orders" -> "M 4 1 L 16 1 L 16 19 L 13 17 L 10 19 L 7 17 L 4 19 Z M 7 6 L 13 6 M 7 10 L 13 10";
            case "arrow" -> "M 2 10 L 18 10 M 12 4 L 18 10 L 12 16";
            default -> "M 10 3 L 10 17 M 3 10 L 17 10";
        });
        path.getStyleClass().add("ui-icon");
        StackPane box = new StackPane(path);
        box.setMinSize(20, 20);
        box.setPrefSize(20, 20);
        box.setMaxSize(20, 20);
        box.setMouseTransparent(true);
        return box;
    }
}
