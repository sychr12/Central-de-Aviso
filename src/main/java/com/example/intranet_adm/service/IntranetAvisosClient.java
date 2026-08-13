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
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntranetAvisosClient {
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:3000";
    private static final String BASE_URL_PREFERENCE = "intranet.base.url";
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(IntranetAvisosClient.class);
    private static final Pattern ONLINE_AGORA_PATTERN = Pattern.compile("\"onlineAgora\"\\s*:\\s*(\\d+)");
    private static final Pattern ACESSOS_HOJE_PATTERN = Pattern.compile("\"acessosHoje\"\\s*:\\s*(\\d+)");
    private static final Pattern TOTAL_VISITANTES_PATTERN = Pattern.compile("\"totalVisitantes\"\\s*:\\s*(\\d+)");
    private static final Pattern ULTIMA_CONEXAO_PATTERN = Pattern.compile("\"ultimaConexao\"\\s*:\\s*\"([^\"]*)\"");
    private static final String DAILY_MESSAGE_PATH = "/api/daily-message";
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024; // precisa bater com o limite do servidor
    private static final Map<String, String> MIME_BY_EXTENSION = Map.of(
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png",
            "gif", "image/gif", "webp", "image/webp");

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    /** Compatibilidade: envia sem imagem. */
    public void enviar(String titulo, String mensagem, String prioridade, String link) throws IOException, InterruptedException {
        enviar(titulo, mensagem, prioridade, link, null);
    }

    /** Envia um aviso; imagem é opcional — passe null pra não anexar nada. */
    public void enviar(String titulo, String mensagem, String prioridade, String link, Path imagem)
            throws IOException, InterruptedException {
        StringBuilder json = new StringBuilder("{");
        json.append("\"title\":\"").append(escape(titulo)).append("\",");
        json.append("\"message\":\"").append(escape(mensagem)).append("\",");
        json.append("\"priority\":\"").append(escape(prioridade)).append("\",");
        json.append("\"link\":\"").append(escape(link)).append("\"");

        if (imagem != null) {
            String mimeType = mimeTypeDe(imagem);
            if (Files.size(imagem) > MAX_IMAGE_BYTES) {
                throw new IOException("Imagem excede o tamanho máximo de 5 MB.");
            }
            String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(imagem));
            json.append(",\"imageBase64\":\"").append(base64).append('"');
            json.append(",\"imageMimeType\":\"").append(mimeType).append('"');
        }
        json.append('}');

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpoint()))
                .timeout(Duration.ofSeconds(20)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8));
        adicionarTokenSeConfigurado(builder);
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Intranet respondeu HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    /** Estatísticas de acesso à intranet, vindas de /api/visitas. */
    public record EstatisticasAcesso(int onlineAgora, int acessosHoje, int totalVisitantes, String ultimaConexao) {}

    /** Consulta quantas pessoas estão vendo a intranet neste momento, e outras estatísticas de acesso. */
    public EstatisticasAcesso buscarEstatisticasAcesso() throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(presenceEndpoint()))
                .timeout(Duration.ofSeconds(8)).GET();
        adicionarTokenSeConfigurado(builder);
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Intranet respondeu HTTP " + response.statusCode() + ": " + response.body());
        }
        String body = response.body();
        return new EstatisticasAcesso(
                extrairInteiro(ONLINE_AGORA_PATTERN, body),
                extrairInteiro(ACESSOS_HOJE_PATTERN, body),
                extrairInteiro(TOTAL_VISITANTES_PATTERN, body),
                extrairTexto(ULTIMA_CONEXAO_PATTERN, body));
    }

    /** Atualiza a mensagem exibida no cartão "Mensagem do dia" da intranet. */
    public void atualizarMensagemDoDia(String mensagem) throws IOException, InterruptedException {
        String body = "{\"message\":\"" + escape(mensagem) + "\"}";
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl() + DAILY_MESSAGE_PATH))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        adicionarTokenSeConfigurado(builder);
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Intranet respondeu HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private static int extrairInteiro(Pattern padrao, String json) throws IOException {
        Matcher m = padrao.matcher(json);
        if (!m.find()) throw new IOException("Resposta de /api/visitas em formato inesperado: " + json);
        return Integer.parseInt(m.group(1));
    }

    private static String extrairTexto(Pattern padrao, String json) {
        Matcher m = padrao.matcher(json);
        return m.find() ? m.group(1) : null; // null quando o campo vem como JSON null (ninguém acessou ainda)
    }

    private static void adicionarTokenSeConfigurado(HttpRequest.Builder builder) {
        String token = System.getenv("INTRANET_AVISOS_TOKEN");
        if (token != null && !token.isBlank()) builder.header("x-intranet-token", token);
    }

    private static String mimeTypeDe(Path imagem) throws IOException {
        String nome = imagem.getFileName().toString().toLowerCase(Locale.ROOT);
        int ponto = nome.lastIndexOf('.');
        String extensao = ponto >= 0 ? nome.substring(ponto + 1) : "";
        String mime = MIME_BY_EXTENSION.get(extensao);
        if (mime == null) {
            throw new IOException("Formato de imagem não suportado: ." + extensao + " (use JPG, PNG, GIF ou WEBP)");
        }
        return mime;
    }

    public static String endpoint() {
        String configured = System.getProperty("intranet.avisos.url", System.getenv("INTRANET_AVISOS_URL"));
        return configured == null || configured.isBlank() ? baseUrl() + "/api/avisos" : validarUrl(configured);
    }

    public static String presenceEndpoint() {
        String configured = System.getProperty("intranet.presence.url", System.getenv("INTRANET_PRESENCE_URL"));
        return configured == null || configured.isBlank() ? baseUrl() + "/api/visitas" : validarUrl(configured);
    }

    /** URL base do site Next.js. Pode ser definida em INTRANET_BASE_URL, -Dintranet.base.url ou pela tela de configurações. */
    public static String baseUrl() {
        String configured = System.getProperty("intranet.base.url");
        if (configured == null || configured.isBlank()) configured = System.getenv("INTRANET_BASE_URL");
        if (configured == null || configured.isBlank()) configured = PREFERENCES.get(BASE_URL_PREFERENCE, null);
        String value = configured == null || configured.isBlank() ? DEFAULT_BASE_URL : configured;
        return validarUrl(value).replaceFirst("/+$", "");
    }

    /** Atualiza o endereço usado pelos dois endpoints durante a execução do aplicativo. */
    public static void configurarBaseUrl(String url) {
        String normalized = validarUrl(url).replaceFirst("/+$", "");
        System.setProperty("intranet.base.url", normalized);
        PREFERENCES.put(BASE_URL_PREFERENCE, normalized);
    }

    private static String validarUrl(String value) {
        URI uri;
        try {
            uri = URI.create(value.trim());
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Endereço da intranet inválido.", error);
        }
        if ((!("http".equalsIgnoreCase(uri.getScheme())) && !("https".equalsIgnoreCase(uri.getScheme())))
                || uri.getHost() == null) {
            throw new IllegalArgumentException("Use uma URL HTTP ou HTTPS válida.");
        }
        return uri.toString();
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
