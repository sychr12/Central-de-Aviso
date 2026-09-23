package com.example.intranet_adm.view.aviso;

import com.example.intranet_adm.view.components.AppIcon;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;

public class AvisoFormImage {

    private final ImageView preview;
    private final Label nomeArquivo;
    private final Button selecionarButton;
    private final Button removerButton;

    private final ObjectProperty<Path> imagemSelecionada = new SimpleObjectProperty<>();

    public AvisoFormImage() {
        preview = new ImageView();
        preview.setFitWidth(194);
        preview.setFitHeight(88);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);

        nomeArquivo = new Label("Nenhuma imagem selecionada");

        selecionarButton = new Button("Selecionar arquivo");
        removerButton = new Button("Remover imagem");

        selecionarButton.getStyleClass().add("secondary-button");
        removerButton.getStyleClass().add("danger-button");

        selecionarButton.setGraphic(AppIcon.create(AppIcon.Type.IMAGE, 17));
        removerButton.setGraphic(AppIcon.create(AppIcon.Type.TRASH, 17));
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
        areaPreview.setMinSize(218, 108);
        areaPreview.setPrefSize(218, 108);
        areaPreview.setMaxSize(218, 108);
        areaPreview.getStyleClass().add("image-preview-box");
        Rectangle recorte = new Rectangle();
        recorte.widthProperty().bind(areaPreview.widthProperty());
        recorte.heightProperty().bind(areaPreview.heightProperty());
        recorte.setArcWidth(20);
        recorte.setArcHeight(20);
        areaPreview.setClip(recorte);

        HBox botoes = new HBox(10);
        botoes.getChildren().addAll(
                selecionarButton,
                removerButton
        );

        nomeArquivo.getStyleClass().add("image-file-name");
        nomeArquivo.setMaxWidth(Double.MAX_VALUE);
        nomeArquivo.setEllipsisString("…");
        VBox detalhes = new VBox(10, nomeArquivo, botoes);
        detalhes.setAlignment(Pos.CENTER_LEFT);
        detalhes.setMinWidth(0);
        HBox layout = new HBox(16, areaPreview, detalhes);
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setMinWidth(0);
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

        imagemSelecionada.set(arquivo.toPath());

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
        imagemSelecionada.set(null);
        preview.setImage(null);
        nomeArquivo.setText("Nenhuma imagem selecionada");
        removerButton.setDisable(true);
    }

    public Path getImagemSelecionada() {
        return imagemSelecionada.get();
    }

    public ReadOnlyObjectProperty<Path> imagemSelecionadaProperty() {
        return imagemSelecionada;
    }

    public ImageView getPreview() {
        return preview;
    }

    public void limpar() {
        removerImagem();
    }
}
