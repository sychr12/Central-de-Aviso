package com.example.intranet_adm.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntranetAvisosClient {

    private static final String DEFAULT_BASE_URL =
            "http://127.0.0.1:3000";

    private static final String BASE_URL_PREFERENCE =
            "intranet.base.url";

    private static final Preferences PREFERENCES =
            Preferences.userNodeForPackage(
                    IntranetAvisosClient.class
            );

    private static final Pattern ONLINE_AGORA_PATTERN =
            Pattern.compile(
                    "\"onlineAgora\"\\s*:\\s*(\\d+)"
            );

    private static final Pattern ACESSOS_HOJE_PATTERN =
            Pattern.compile(
                    "\"acessosHoje\"\\s*:\\s*(\\d+)"
            );

    private static final Pattern TOTAL_VISITANTES_PATTERN =
            Pattern.compile(
                    "\"totalVisitantes\"\\s*:\\s*(\\d+)"
            );

    private static final Pattern ULTIMA_CONEXAO_PATTERN =
            Pattern.compile(
                    "\"ultimaConexao\"\\s*:\\s*\"([^\"]*)\""
            );

    private static final String DAILY_MESSAGE_PATH =
            "/api/daily-message";

    private static final long MAX_IMAGE_BYTES =
            5L * 1024 * 1024;

    private static final Map<String, String> MIME_BY_EXTENSION =
            Map.of(
                    "jpg", "image/jpeg",
                    "jpeg", "image/jpeg",
                    "png", "image/png",
                    "gif", "image/gif",
                    "webp", "image/webp"
            );

    private static final DateTimeFormatter
            ISO_LOCAL_DATE_TIME =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final HttpClient client =
            HttpClient.newBuilder()
                    .connectTimeout(
                            Duration.ofSeconds(5)
                    )
                    .followRedirects(
                            HttpClient.Redirect.NORMAL
                    )
                    .version(
                            HttpClient.Version.HTTP_1_1
                    )
                    .build();

    // ============================================================
    // ENVIO COMPATÍVEL SEM IMAGEM E SEM DATAS
    // ============================================================

    /**
     * Compatibilidade:
     * envia aviso sem imagem e sem datas.
     */
    public void enviar(
            String titulo,
            String mensagem,
            String prioridade,
            String link
    ) throws IOException, InterruptedException {

        enviar(
                titulo,
                mensagem,
                prioridade,
                link,
                null,
                null,
                null
        );
    }

    /**
     * Compatibilidade:
     * envia aviso com imagem, mas sem datas.
     */
    public void enviar(
            String titulo,
            String mensagem,
            String prioridade,
            String link,
            Path imagem
    ) throws IOException, InterruptedException {

        enviar(
                titulo,
                mensagem,
                prioridade,
                link,
                imagem,
                null,
                null
        );
    }

    // ============================================================
    // NOVO ENVIO COM PUBLICAÇÃO E EXPIRAÇÃO
    // ============================================================

    /**
     * Envia aviso completo para a Intranet.
     *
     * @param titulo título do aviso
     * @param mensagem mensagem do aviso
     * @param prioridade prioridade
     * @param link link opcional
     * @param imagem imagem opcional
     * @param publicarEm data/hora de publicação
     * @param expirarEm data/hora de expiração
     */
    public void enviar(
            String titulo,
            String mensagem,
            String prioridade,
            String link,
            Path imagem,
            LocalDateTime publicarEm,
            LocalDateTime expirarEm
    ) throws IOException, InterruptedException {

        if (
                titulo == null
                        || titulo.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Título é obrigatório."
            );
        }

        if (
                mensagem == null
                        || mensagem.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Mensagem é obrigatória."
            );
        }

        if (
                publicarEm != null
                        && expirarEm != null
                        && !expirarEm.isAfter(publicarEm)
        ) {
            throw new IllegalArgumentException(
                    "A expiração deve ser depois da publicação."
            );
        }

        StringBuilder json =
                new StringBuilder(512);

        json.append('{');

        // --------------------------------------------------------
        // CAMPOS BÁSICOS
        // --------------------------------------------------------

        appendJsonField(
                json,
                "title",
                titulo,
                true
        );

        appendJsonField(
                json,
                "message",
                mensagem,
                false
        );

        appendJsonField(
                json,
                "priority",
                prioridade,
                false
        );

        appendJsonField(
                json,
                "link",
                link,
                false
        );

        // --------------------------------------------------------
        // DATA DE PUBLICAÇÃO
        // --------------------------------------------------------

        String publishedAt =
                publicarEm != null
                        ? publicarEm.format(
                        ISO_LOCAL_DATE_TIME
                )
                        : null;

        appendJsonField(
                json,
                "publishedAt",
                publishedAt,
                false
        );

        // --------------------------------------------------------
        // DATA DE EXPIRAÇÃO
        // --------------------------------------------------------

        String expiresAt =
                expirarEm != null
                        ? expirarEm.format(
                        ISO_LOCAL_DATE_TIME
                )
                        : null;

        appendJsonField(
                json,
                "expiresAt",
                expiresAt,
                false
        );

        // --------------------------------------------------------
        // IMAGEM
        // --------------------------------------------------------

        if (imagem != null) {

            if (!Files.exists(imagem)) {
                throw new IOException(
                        "A imagem selecionada não existe."
                );
            }

            String mimeType =
                    mimeTypeDe(imagem);

            long tamanho =
                    Files.size(imagem);

            if (
                    tamanho > MAX_IMAGE_BYTES
            ) {

                throw new IOException(
                        "Imagem excede o tamanho máximo de 5 MB "
                                + "(tem "
                                + (tamanho / (1024 * 1024))
                                + " MB)."
                );
            }

            byte[] bytes =
                    Files.readAllBytes(imagem);

            String base64 =
                    Base64.getEncoder()
                            .encodeToString(bytes);

            appendJsonField(
                    json,
                    "imageBase64",
                    base64,
                    false
            );

            appendJsonField(
                    json,
                    "imageMimeType",
                    mimeType,
                    false
            );
        }

        json.append('}');

        // ========================================================
        // REQUISIÇÃO
        // ========================================================

        HttpRequest.Builder builder =
                HttpRequest.newBuilder(
                                URI.create(endpoint())
                        )
                        .timeout(
                                Duration.ofSeconds(20)
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        json.toString(),
                                        StandardCharsets.UTF_8
                                )
                        );

        enviarRequisicao(builder);
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public record EstatisticasAcesso(
            int onlineAgora,
            int acessosHoje,
            int totalVisitantes,
            String ultimaConexao
    ) {
    }

    public EstatisticasAcesso
    buscarEstatisticasAcesso()
            throws IOException, InterruptedException {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder(
                                URI.create(
                                        presenceEndpoint()
                                )
                        )
                        .timeout(
                                Duration.ofSeconds(8)
                        )
                        .GET();

        String body =
                enviarRequisicao(builder)
                        .body();

        return new EstatisticasAcesso(
                extrairInteiro(
                        ONLINE_AGORA_PATTERN,
                        body
                ),
                extrairInteiro(
                        ACESSOS_HOJE_PATTERN,
                        body
                ),
                extrairInteiro(
                        TOTAL_VISITANTES_PATTERN,
                        body
                ),
                extrairTexto(
                        ULTIMA_CONEXAO_PATTERN,
                        body
                )
        );
    }

    // ============================================================
    // MENSAGEM DO DIA
    // ============================================================

    public void atualizarMensagemDoDia(
            String mensagem
    ) throws IOException, InterruptedException {

        if (mensagem == null) {
            throw new IllegalArgumentException(
                    "Mensagem não pode ser nula."
            );
        }

        StringBuilder json =
                new StringBuilder(64);

        json.append('{');

        appendJsonField(
                json,
                "message",
                mensagem,
                true
        );

        json.append('}');

        HttpRequest.Builder builder =
                HttpRequest.newBuilder(
                                URI.create(
                                        baseUrl()
                                                + DAILY_MESSAGE_PATH
                                )
                        )
                        .timeout(
                                Duration.ofSeconds(20)
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        json.toString(),
                                        StandardCharsets.UTF_8
                                )
                        );

        enviarRequisicao(builder);
    }

    // ============================================================
    // REQUISIÇÃO
    // ============================================================

    private HttpResponse<String>
    enviarRequisicao(
            HttpRequest.Builder builder
    ) throws IOException, InterruptedException {

        adicionarTokenSeConfigurado(builder);

        HttpResponse<String> response =
                client.send(
                        builder.build(),
                        HttpResponse.BodyHandlers.ofString(
                                StandardCharsets.UTF_8
                        )
                );

        if (
                response.statusCode() < 200
                        || response.statusCode() >= 300
        ) {

            throw new IOException(
                    "Intranet respondeu HTTP "
                            + response.statusCode()
                            + ": "
                            + response.body()
            );
        }

        return response;
    }

    // ============================================================
    // JSON / ESTATÍSTICAS
    // ============================================================

    private static int extrairInteiro(
            Pattern padrao,
            String json
    ) throws IOException {

        Matcher matcher =
                padrao.matcher(json);

        if (!matcher.find()) {

            throw new IOException(
                    "Resposta de /api/visitas em formato inesperado: "
                            + json
            );
        }

        return Integer.parseInt(
                matcher.group(1)
        );
    }

    private static String extrairTexto(
            Pattern padrao,
            String json
    ) {

        Matcher matcher =
                padrao.matcher(json);

        return matcher.find()
                ? matcher.group(1)
                : null;
    }

    private static void
    adicionarTokenSeConfigurado(
            HttpRequest.Builder builder
    ) {

        String token =
                System.getenv(
                        "INTRANET_AVISOS_TOKEN"
                );

        if (
                token != null
                        && !token.isBlank()
        ) {

            builder.header(
                    "x-intranet-token",
                    token
            );
        }
    }

    // ============================================================
    // IMAGEM
    // ============================================================

    private static String mimeTypeDe(
            Path imagem
    ) throws IOException {

        String nome =
                imagem.getFileName()
                        .toString()
                        .toLowerCase(
                                Locale.ROOT
                        );

        int ponto =
                nome.lastIndexOf('.');

        String extensao =
                ponto >= 0
                        ? nome.substring(
                        ponto + 1
                )
                        : "";

        String mime =
                MIME_BY_EXTENSION.get(
                        extensao
                );

        if (mime == null) {

            throw new IOException(
                    "Formato de imagem não suportado: ."
                            + extensao
                            + " (use JPG, PNG, GIF ou WEBP)"
            );
        }

        return mime;
    }

    // ============================================================
    // ENDPOINTS
    // ============================================================

    public static String endpoint() {

        String configured =
                System.getProperty(
                        "intranet.avisos.url",
                        System.getenv(
                                "INTRANET_AVISOS_URL"
                        )
                );

        return configured == null
                || configured.isBlank()
                ? baseUrl() + "/api/avisos"
                : validarUrl(configured);
    }

    public static String presenceEndpoint() {

        String configured =
                System.getProperty(
                        "intranet.presence.url",
                        System.getenv(
                                "INTRANET_PRESENCE_URL"
                        )
                );

        return configured == null
                || configured.isBlank()
                ? baseUrl() + "/api/visitas"
                : validarUrl(configured);
    }

    // ============================================================
    // BASE URL
    // ============================================================

    public static String baseUrl() {

        String configured =
                System.getProperty(
                        "intranet.base.url"
                );

        if (
                configured == null
                        || configured.isBlank()
        ) {

            configured =
                    System.getenv(
                            "INTRANET_BASE_URL"
                    );
        }

        if (
                configured == null
                        || configured.isBlank()
        ) {

            configured =
                    PREFERENCES.get(
                            BASE_URL_PREFERENCE,
                            null
                    );
        }

        String value =
                configured == null
                        || configured.isBlank()
                        ? DEFAULT_BASE_URL
                        : configured;

        return validarUrl(value)
                .replaceFirst(
                        "/+$",
                        ""
                );
    }

    public static void configurarBaseUrl(
            String url
    ) {

        String normalized =
                validarUrl(url)
                        .replaceFirst(
                                "/+$",
                                ""
                        );

        System.setProperty(
                "intranet.base.url",
                normalized
        );

        PREFERENCES.put(
                BASE_URL_PREFERENCE,
                normalized
        );
    }

    private static String validarUrl(
            String value
    ) {

        URI uri;

        try {

            uri =
                    URI.create(
                            value.trim()
                    );

        } catch (IllegalArgumentException error) {

            throw new IllegalArgumentException(
                    "Endereço da intranet inválido.",
                    error
            );
        }

        boolean http =
                "http".equalsIgnoreCase(
                        uri.getScheme()
                );

        boolean https =
                "https".equalsIgnoreCase(
                        uri.getScheme()
                );

        if (
                (!http && !https)
                        || uri.getHost() == null
        ) {

            throw new IllegalArgumentException(
                    "Use uma URL HTTP ou HTTPS válida."
            );
        }

        return uri.toString();
    }

    // ============================================================
    // JSON
    // ============================================================

    private static void appendJsonField(
            StringBuilder json,
            String key,
            String value,
            boolean primeiroCampo
    ) {

        if (!primeiroCampo) {
            json.append(',');
        }

        json.append('"')
                .append(
                        escape(key)
                )
                .append("\":");

        if (value == null) {

            json.append("null");

        } else {

            json.append('"')
                    .append(
                            escape(value)
                    )
                    .append('"');
        }
    }

    private static String escape(
            String value
    ) {

        StringBuilder escaped =
                new StringBuilder(
                        value.length() + 16
                );

        for (
                int index = 0;
                index < value.length();
                index++
        ) {

            char character =
                    value.charAt(index);

            switch (character) {

                case '"' ->
                        escaped.append("\\\"");

                case '\\' ->
                        escaped.append("\\\\");

                case '\b' ->
                        escaped.append("\\b");

                case '\f' ->
                        escaped.append("\\f");

                case '\n' ->
                        escaped.append("\\n");

                case '\r' ->
                        escaped.append("\\r");

                case '\t' ->
                        escaped.append("\\t");

                default -> {

                    if (character < 0x20) {

                        escaped.append(
                                String.format(
                                        "\\u%04x",
                                        (int) character
                                )
                        );

                    } else {

                        escaped.append(
                                character
                        );
                    }
                }
            }
        }

        return escaped.toString();
    }
}