package com.example.intranet_adm.view.components;

import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Carrega imagens dos avisos sem bloquear a interface. */
public final class PopupMediaView {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            3,
            tarefa -> {
                Thread thread = new Thread(tarefa, "popup-image-loader");
                thread.setDaemon(true);
                return thread;
            });

    private PopupMediaView() {
    }

    public static Node criar(
            String origem,
            double largura,
            double altura,
            String classeCss) {

        StackPane area = new StackPane();
        area.setMinHeight(altura + 10);
        area.setPrefHeight(altura + 10);
        area.setMaxWidth(largura + 10);
        area.getStyleClass().add(classeCss);

        Label estado = new Label("Carregando imagem...");
        estado.getStyleClass().add("popup-library-media-state");
        area.getChildren().add(estado);

        Image armazenada = CACHE.get(origem);
        if (armazenada != null) {
            exibir(area, armazenada, largura, altura);
            return area;
        }

        Task<byte[]> carregamento = new Task<>() {
            @Override
            protected byte[] call() throws Exception {
                return baixar(origem);
            }
        };
        carregamento.setOnSucceeded(event -> {
            byte[] bytes = carregamento.getValue();
            Image imagem = new Image(
                    new ByteArrayInputStream(bytes),
                    largura, altura, true, true);
            if (imagem.isError()) {
                mostrarErro(area);
                return;
            }
            CACHE.put(origem, imagem);
            exibir(area, imagem, largura, altura);
        });
        carregamento.setOnFailed(event -> {
            Throwable erro = carregamento.getException();
            System.err.println("Não foi possível carregar a imagem "
                    + origem + ": "
                    + (erro == null ? "erro desconhecido" : erro.getMessage()));
            mostrarErro(area);
        });
        EXECUTOR.execute(carregamento);
        return area;
    }

    public static boolean ehImagem(String origem, String mimeType) {
        if (mimeType != null
                && mimeType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return true;
        }
        if (origem == null || origem.isBlank()) return false;
        String valor = origem.toLowerCase(Locale.ROOT);
        if (valor.startsWith("data:image/")) return true;
        int separador = valor.indexOf('?');
        if (separador >= 0) valor = valor.substring(0, separador);
        separador = valor.indexOf('#');
        if (separador >= 0) valor = valor.substring(0, separador);
        return valor.endsWith(".png") || valor.endsWith(".jpg")
                || valor.endsWith(".jpeg") || valor.endsWith(".gif")
                || valor.endsWith(".webp") || valor.endsWith(".bmp");
    }

    private static byte[] baixar(String origem) throws Exception {
        if (origem == null || origem.isBlank()) {
            throw new IllegalArgumentException("Imagem sem endereço.");
        }
        if (origem.startsWith("data:")) {
            int virgula = origem.indexOf(',');
            if (virgula < 0) throw new IllegalArgumentException("Imagem inválida.");
            return Base64.getDecoder().decode(origem.substring(virgula + 1));
        }

        URI uri = URI.create(origem);
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return Files.readAllBytes(Path.of(uri));
        }

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0 Central-de-Avisos/1.0")
                .header("Accept", "image/avif,image/webp,image/*,*/*;q=0.8")
                .GET()
                .build();
        HttpResponse<byte[]> response = HTTP.send(
                request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Servidor respondeu HTTP " + response.statusCode());
        }
        return response.body();
    }

    private static void exibir(
            StackPane area, Image imagem, double largura, double altura) {
        ImageView visualizacao = new ImageView(imagem);
        visualizacao.setFitWidth(largura);
        visualizacao.setFitHeight(altura);
        visualizacao.setPreserveRatio(true);
        visualizacao.setSmooth(true);
        area.getChildren().setAll(visualizacao);
    }

    private static void mostrarErro(StackPane area) {
        Label estado = new Label("Imagem indisponível");
        estado.getStyleClass().add("popup-library-media-state");
        area.getChildren().setAll(estado);
    }
}
