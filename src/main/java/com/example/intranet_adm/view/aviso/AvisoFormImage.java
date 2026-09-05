package com.example.intranet_adm.view.aviso;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;

public class AvisoFormImage {

    private final ImageView preview;
    private final Label nomeArquivo;
    private final Button selecionarButton;
    private final Button removerButton;

    private Path imagemSelecionada;

    public AvisoFormImage() {
        preview = new ImageView();
        preview.setFitWidth(214);
        preview.setFitHeight(124);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);

        nomeArquivo = new Label("Nenhuma imagem selecionada");

        selecionarButton = new Button("Selecionar arquivo");
        removerButton = new Button("Remover imagem");

        selecionarButton.getStyleClass().add("secondary-button");
        removerButton.getStyleClass().add("danger-button");

        removerButton.setDisable(true);

        selecionarButton.setOnAction(event -> selecionarImagem(null));
        removerButton.setOnAction(event -> removerImagem());
    }

    public VBox criarLayout() {
        VBox container = new VBox(10);

        Label placeholder = new Label("Nenhuma imagem selecionada");
        placeholder.getStyleClass().add("image-placeholder");
        placeholder.visibleProperty().bind(preview.imageProperty().isNull());
        placeholder.managedProperty().bind(placeholder.visibleProperty());

        StackPane areaPreview = new StackPane(placeholder, preview);
        areaPreview.setAlignment(Pos.CENTER);
        areaPreview.setPrefSize(230, 140);
        areaPreview.setMinSize(230, 140);
        areaPreview.getStyleClass().add("image-preview-box");

        HBox botoes = new HBox(10);
        botoes.getChildren().addAll(
                selecionarButton,
                removerButton
        );

        nomeArquivo.getStyleClass().add("image-file-name");
        VBox detalhes = new VBox(10, nomeArquivo, botoes);
        detalhes.setAlignment(Pos.CENTER_LEFT);
        HBox layout = new HBox(16, areaPreview, detalhes);
        layout.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(detalhes, Priority.ALWAYS);
        container.getChildren().add(layout);

        return container;
    }

    public void selecionarImagem(Window owner) {
        FileChooser chooser = new FileChooser();

        chooser.setTitle("Selecionar imagem");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Imagens e documentos",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.gif",
                        "*.webp",
                        "*.pdf"
                )
        );

        File arquivo = chooser.showOpenDialog(owner);

        if (arquivo == null) {
            return;
        }

        imagemSelecionada = arquivo.toPath();

        if (!arquivo.getName().toLowerCase().endsWith(".pdf")) {
            preview.setImage(new Image(arquivo.toURI().toString()));
        } else {
            preview.setImage(null);
        }

        nomeArquivo.setText(
                arquivo.getName()
        );

        removerButton.setDisable(false);
    }

    public void removerImagem() {
        imagemSelecionada = null;
        preview.setImage(null);
        nomeArquivo.setText("Nenhuma imagem selecionada");
        removerButton.setDisable(true);
    }

    public Path getImagemSelecionada() {
        return imagemSelecionada;
    }

    public ImageView getPreview() {
        return preview;
    }

    public void limpar() {
        removerImagem();
    }
}
