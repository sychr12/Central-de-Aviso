package com.example.intranet_adm.view.aviso;

import com.example.intranet_adm.service.IntranetAvisosClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AvisoFormView {

    private final AvisoFormFields fields;
    private final AvisoFormDates dates;
    private final AvisoFormImage image;
    private final AvisoFormValidation validation;
    private final AvisoFormActions actions;

    private final VBox root;

    public AvisoFormView() {

        IntranetAvisosClient client =
                new IntranetAvisosClient();

        fields = new AvisoFormFields();
        dates = new AvisoFormDates();
        image = new AvisoFormImage();
        validation = new AvisoFormValidation();

        actions = new AvisoFormActions(
                fields,
                dates,
                image,
                validation,
                client
        );

        root = criarView();
    }

    private VBox criarView() {

        VBox container = new VBox(18);
        container.setMaxWidth(1160);

        VBox detalhes = criarCard("Detalhes do aviso", fields.criarLayout(), dates.criarLayout());

        VBox imagem = criarCard("Imagem (opcional)", image.criarLayout());

        Button limparButton = new Button("Limpar");
        limparButton.getStyleClass().add("secondary-button");
        limparButton.setOnAction(event -> {
            fields.limpar();
            dates.limpar();
            image.limpar();
        });

        Button enviarButton = new Button("Enviar aviso");

        enviarButton.getStyleClass()
                .add("primary-button");

        enviarButton.setOnAction(
                event -> actions.enviar()
        );

        HBox botoes = new HBox(10, limparButton, enviarButton);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        container.getChildren().addAll(detalhes, imagem, botoes);

        return container;
    }

    public Node getView() {
        return root;
    }

    public VBox getRoot() {
        return root;
    }

    public AvisoFormFields getFields() {
        return fields;
    }

    public AvisoFormDates getDates() {
        return dates;
    }

    public AvisoFormImage getImage() {
        return image;
    }

    public AvisoFormValidation getValidation() {
        return validation;
    }

    public AvisoFormActions getActions() {
        return actions;
    }

    public static Node criar() {
        return new AvisoFormView().getView();
    }

    private VBox criarCard(String titulo, Node... conteudo) {
        Label heading = new Label(titulo);
        heading.getStyleClass().add("card-title");
        VBox card = new VBox(14);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("app-card");
        card.getChildren().add(heading);
        card.getChildren().addAll(conteudo);
        return card;
    }

}
