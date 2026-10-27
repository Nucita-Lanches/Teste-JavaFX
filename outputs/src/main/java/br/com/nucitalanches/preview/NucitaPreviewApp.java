package br.com.nucitalanches.preview;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NucitaPreviewApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                NucitaPreviewApp.class.getResource("/view/exemplo-cardapio.fxml"));
        Scene scene = new Scene(loader.load(), 1440, 900);
        stage.setTitle("Nucita Lanches | Atendimento");
        stage.setMinWidth(1220);
        stage.setMinHeight(730);
        stage.getIcons().add(new javafx.scene.image.Image(
                NucitaPreviewApp.class.getResourceAsStream("/images/logo.png")));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
