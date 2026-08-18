/// Olá! Este arquivo é a ponte entre a aplicação e o servidor da Intranet.
/// Ele é responsável pelas requisições HTTP, envio e recebimento de dados.
/// Alterações nos endpoints, formato das requisições ou respostas do servidor
/// também devem ser refletidas neste arquivo. =)
///
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> MIME_BY_EXTENSION = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp"
    );

    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public void removerMensagemDoDia(String mensagem)
            throws IOException, InterruptedException {

        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException(
                    "Mensagem não pode ser vazia."
            );
        }

        StringBuilder json = new StringBuilder(128);
        json.append('{');

        appendJsonField(
                json,
                "message",
                mensagem.trim(),
                true
        );

        json.append('}');

        HttpRequest.Builder builder =
                HttpRequest.newBuilder(
                                URI.create(
                                        baseUrl() + DAILY_MESSAGE_PATH
                                )
                        )
                        .timeout(Duration.ofSeconds(20))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .method(
                                "DELETE",
                                HttpRequest.BodyPublishers.ofString(
                                        json.toString(),
                                        StandardCharsets.UTF_8
                                )
                        );

        HttpResponse<String> response =
                enviarRequisicao(builder);

        System.out.println(
                "Mensagem do dia removida com sucesso: "
                        + mensagem
        );

        System.out.println(
                "Resposta do servidor: "
                        + response.body()
        );
    }

    // ============================================================
    // CLASSE DE CONFIGURAÇÃO (Builder Pattern)
    //
    // Reflete apenas os campos que o formulário "Novo Aviso" da
    // Central de Avisos envia hoje. Modelo, Tamanho, Ícone,
    // Mostrar datas, cores e Páginas foram removidos da tela e,
    // por isso, também não fazem mais parte do envio.
    // ============================================================

    public static class AvisoConfig {
        private final String titulo;
        private final String mensagem;
        private String prioridade = "normal";
        private String link = null;
        private Path imagem = null;
        private LocalDateTime publicarEm = null;
        private LocalDateTime expirarEm = null;
        private boolean ativo = true;
        private boolean podeFechar = true;

        public AvisoConfig(String titulo, String mensagem) {
            if (titulo == null || titulo.isBlank()) {
                throw new IllegalArgumentException("Título é obrigatório");
            }
            if (mensagem == null || mensagem.isBlank()) {
                throw new IllegalArgumentException("Mensagem é obrigatória");
            }
            this.titulo = titulo;
            this.mensagem = mensagem;
        }

        public AvisoConfig comPrioridade(String prioridade) {
            this.prioridade = prioridade != null ? prioridade : "normal";
            return this;
        }

        public AvisoConfig comLink(String link) {
            this.link = link;
            return this;
        }

        public AvisoConfig comImagem(Path imagem) {
            this.imagem = imagem;
            return this;
        }

        public AvisoConfig comPublicarEm(LocalDateTime publicarEm) {
            this.publicarEm = publicarEm;
            return this;
        }

        public AvisoConfig comExpirarEm(LocalDateTime expirarEm) {
            this.expirarEm = expirarEm;
            return this;
        }

        public AvisoConfig comAtivo(boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public AvisoConfig comPodeFechar(boolean podeFechar) {
            this.podeFechar = podeFechar;
            return this;
        }

        // Getters
        public String getTitulo() { return titulo; }
        public String getMensagem() { return mensagem; }
        public String getPrioridade() { return prioridade; }
        public String getLink() { return link; }
        public Path getImagem() { return imagem; }
        public LocalDateTime getPublicarEm() { return publicarEm; }
        public LocalDateTime getExpirarEm() { return expirarEm; }
        public boolean isAtivo() { return ativo; }
        public boolean isPodeFechar() { return podeFechar; }
    }

    // ============================================================
    // MÉTODOS DE ENVIO
    // ============================================================

    public void enviar(AvisoConfig config) throws IOException, InterruptedException {
        if (config.getPublicarEm() != null && config.getExpirarEm() != null
                && !config.getExpirarEm().isAfter(config.getPublicarEm())) {
            throw new IllegalArgumentException("A expiração deve ser depois da publicação.");
        }

        StringBuilder json = new StringBuilder(512);
        json.append('{');

        appendJsonField(json, "title", config.getTitulo(), true);
        appendJsonField(json, "message", config.getMensagem(), false);
        appendJsonField(json, "priority", config.getPrioridade(), false);
        appendJsonField(json, "link", config.getLink(), false);

        if (config.getPublicarEm() != null) {
            appendJsonField(json, "publishedDate", config.getPublicarEm().format(ISO_LOCAL_DATE_TIME), false);
        }

        if (config.getExpirarEm() != null) {
            appendJsonField(json, "expirationDate", config.getExpirarEm().format(ISO_LOCAL_DATE_TIME), false);
        }

        appendJsonBooleanField(json, "active", config.isAtivo(), false);
        appendJsonBooleanField(json, "closable", config.isPodeFechar(), false);

        if (config.getImagem() != null) {
            Path imagem = config.getImagem();
            if (!Files.exists(imagem)) {
                throw new IOException("A imagem selecionada não existe.");
            }

            String mimeType = mimeTypeDe(imagem);
            long tamanhoArquivo = Files.size(imagem);
            if (tamanhoArquivo > MAX_IMAGE_BYTES) {
                throw new IOException("Imagem excede o tamanho máximo de 5 MB.");
            }

            byte[] bytes = Files.readAllBytes(imagem);
            String base64 = Base64.getEncoder().encodeToString(bytes);
            appendJsonField(json, "imageBase64", base64, false);
            appendJsonField(json, "imageMimeType", mimeType, false);
        }

        json.append('}');

        System.out.println("Enviando JSON para Next.js: " + json.toString());

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpoint()))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8));

        enviarRequisicao(builder);
    }


    public void alterarStatusPopup(String id, boolean ativo)
            throws IOException, InterruptedException {

        validarPopupId(id);

        String json = "{\"active\":" + ativo + "}";

        String url = endpoint()
                + "?id="
                + java.net.URLEncoder.encode(id, StandardCharsets.UTF_8);

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .method(
                        "PATCH",
                        HttpRequest.BodyPublishers.ofString(
                                json,
                                StandardCharsets.UTF_8
                        )
                );

        enviarRequisicao(builder);

        System.out.println(
                "Popup " + id +
                        (ativo ? " ativado" : " desativado") +
                        " com sucesso!"
        );
    }



    public void enviar(String titulo, String mensagem) throws IOException, InterruptedException {
        enviar(new AvisoConfig(titulo, mensagem));
    }

    // ============================================================
    // GERENCIAMENTO DE POPUPS - COM SUPORTE A PATCH E DELETE
    // ============================================================

    public record Popup(String id, String titulo, String mensagem, boolean ativo,
                        String modelo, String tamanho, String paginas) {}

    /**
     * Lista todos os popups do servidor
     */
    public List<Popup> listarPopups()
            throws IOException, InterruptedException {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder(
                                URI.create(endpoint())
                        )
                        .timeout(Duration.ofSeconds(10))
                        .GET();

        HttpResponse<String> response =
                enviarRequisicao(builder);

        String json = response.body();

        System.out.println("================================");
        System.out.println("GET /api/avisos");
        System.out.println("HTTP: " + response.statusCode());
        System.out.println("RESPOSTA:");
        System.out.println(json);
        System.out.println("================================");

        List<Popup> popups =
                parsePopupUnico(json);

        System.out.println(
                "Popups encontrados: "
                        + popups.size()
        );

        return popups;
    }

    /**
     * Ativa um popup (PATCH /api/avisos/:id)
     */
    public void ativarPopup(String id) throws IOException, InterruptedException {
        validarPopupId(id);

        String json = "{\"active\":true}";
        HttpRequest.Builder builder = HttpRequest.newBuilder(
                        URI.create(endpoint() + "?id=" + java.net.URLEncoder.encode(id, StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));

        enviarRequisicao(builder);
        System.out.println("Popup " + id + " ativado com sucesso!");
    }

    public void desativarPopup(String id) throws IOException, InterruptedException {
        validarPopupId(id);

        String json = "{\"active\":false}";
        HttpRequest.Builder builder = HttpRequest.newBuilder(
                        URI.create(endpoint() + "?id=" + java.net.URLEncoder.encode(id, StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));

        enviarRequisicao(builder);
        System.out.println("Popup " + id + " desativado com sucesso!");
    }

    public void excluirPopup(String id) throws IOException, InterruptedException {
        validarPopupId(id);

        HttpRequest.Builder builder = HttpRequest.newBuilder(
                        URI.create(endpoint() + "?id=" + java.net.URLEncoder.encode(id, StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(10))
                .DELETE();

        enviarRequisicao(builder);
        System.out.println("Popup " + id + " excluído com sucesso!");
    }

    /**
     * Apaga um popup (alias para excluirPopup)
     */
    public void apagarPopup(String id) throws IOException, InterruptedException {
        excluirPopup(id);
    }

    private static void validarPopupId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID do popup é obrigatório.");
        }
    }

    private List<Popup> parsePopupUnico(String json) {
        List<Popup> resultado = new ArrayList<>();

        if (json == null || json.isBlank()) {
            return resultado;
        }

        String texto = json.trim();

        try {
            // Resposta com apenas um popup
            if (texto.startsWith("{")) {
                Popup popup = parsePopupObject(texto);

                if (popup != null) {
                    resultado.add(popup);
                }

                return resultado;
            }

            // Resposta com vários popups
            if (texto.startsWith("[")) {
                List<String> objetos =
                        extrairObjetosJson(texto);

                for (String objeto : objetos) {
                    Popup popup =
                            parsePopupObject(objeto);

                    if (popup != null) {
                        resultado.add(popup);
                    }
                }

                return resultado;
            }

            System.err.println(
                    "JSON de /api/avisos não reconhecido: "
                            + texto
            );

        } catch (Exception e) {
            System.err.println(
                    "Erro ao processar popups: "
                            + e.getMessage()
            );
        }

        return resultado;
    }

    private List<String> extrairObjetosJson(String json) {
        List<String> objetos = new ArrayList<>();

        int nivel = 0;
        int inicio = -1;

        boolean dentroString = false;
        boolean escape = false;

        for (int i = 0; i < json.length(); i++) {
            char caractere = json.charAt(i);

            if (dentroString) {
                if (escape) {
                    escape = false;
                    continue;
                }

                if (caractere == '\\') {
                    escape = true;
                    continue;
                }

                if (caractere == '"') {
                    dentroString = false;
                }

                continue;
            }

            if (caractere == '"') {
                dentroString = true;
                continue;
            }

            if (caractere == '{') {
                if (nivel == 0) {
                    inicio = i;
                }

                nivel++;
                continue;
            }

            if (caractere == '}') {
                nivel--;

                if (nivel == 0 && inicio >= 0) {
                    objetos.add(
                            json.substring(
                                    inicio,
                                    i + 1
                            )
                    );

                    inicio = -1;
                }
            }
        }

        return objetos;
    }

    private Popup parsePopupObject(String objeto) {
        try {
            String id = extrairCampoTexto(objeto, "id");
            if (id == null) id = extrairCampoNumero(objeto, "id");
            if (id == null) id = String.valueOf(System.currentTimeMillis());

            String titulo = extrairCampoTexto(objeto, "title");
            if (titulo == null) titulo = extrairCampoTexto(objeto, "titulo");
            if (titulo == null) titulo = "Sem título";

            String mensagem = extrairCampoTexto(objeto, "message");
            if (mensagem == null) mensagem = extrairCampoTexto(objeto, "mensagem");
            if (mensagem == null) mensagem = "";

            // --------------------------------------------------------
            // STATUS ATIVO/INATIVO
            // Usa o campo "active"/"ativo" retornado pelo backend quando
            // presente (fonte de verdade real, refletindo ativarPopup/
            // desativarPopup). Se o backend não enviar o campo, assume
            // ativo=true por padrão. A expiração por data sempre pode
            // sobrepor para false, mesmo que o backend diga ativo.
            // --------------------------------------------------------
            boolean ativo;
            boolean temCampoActive = extrairCampoTexto(objeto, "active") != null
                    || extrairCampoTexto(objeto, "ativo") != null;

            if (temCampoActive) {
                ativo = extrairCampoBoolean(objeto, "active", true)
                        && extrairCampoBoolean(objeto, "ativo", true);
            } else {
                ativo = true;
            }

            String expirationDate = extrairCampoTexto(objeto, "expirationDate");
            if (expirationDate != null) {
                try {
                    java.time.Instant instant = java.time.Instant.parse(expirationDate);
                    if (java.time.Instant.now().isAfter(instant)) {
                        ativo = false;
                    }
                } catch (Exception e) {
                    // Ignora erro de parsing
                }
            }

            // Modelo, tamanho e páginas não são mais definidos ao criar um
            // aviso novo, mas popups antigos gravados no servidor ainda
            // podem trazer esses campos — mantemos a leitura aqui apenas
            // para exibição no histórico/lista de popups existentes.
            String modelo = extrairCampoTexto(objeto, "model");
            if (modelo == null) modelo = extrairCampoTexto(objeto, "modelo");
            if (modelo == null) modelo = "Popup";

            String tamanho = extrairCampoTexto(objeto, "size");
            if (tamanho == null) tamanho = extrairCampoTexto(objeto, "tamanho");
            if (tamanho == null) tamanho = "Médio";

            StringBuilder paginas = new StringBuilder();
            if (paginas.isEmpty()) paginas.append("Nenhuma");

            return new Popup(
                    id,
                    titulo,
                    mensagem,
                    ativo,
                    modelo,
                    tamanho,
                    paginas.toString()
            );
        } catch (Exception e) {
            System.err.println("Erro ao parsear objeto: " + objeto);
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================


    public record EstatisticasAcesso(int onlineAgora, int acessosHoje, int totalVisitantes, String ultimaConexao) {}

    public EstatisticasAcesso buscarEstatisticasAcesso() throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(presenceEndpoint()))
                .timeout(Duration.ofSeconds(8))
                .GET();

        String body = enviarRequisicao(builder).body();

        return new EstatisticasAcesso(
                extrairInteiro(ONLINE_AGORA_PATTERN, body),
                extrairInteiro(ACESSOS_HOJE_PATTERN, body),
                extrairInteiro(TOTAL_VISITANTES_PATTERN, body),
                extrairTexto(ULTIMA_CONEXAO_PATTERN, body)
        );
    }

    // ============================================================
    // MENSAGEM DO DIA
    // ============================================================

    public void atualizarMensagemDoDia(String mensagem) throws IOException, InterruptedException {
        if (mensagem == null) {
            throw new IllegalArgumentException("Mensagem não pode ser nula.");
        }

        StringBuilder json = new StringBuilder(64);
        json.append('{');
        appendJsonField(json, "message", mensagem, true);
        json.append('}');

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl() + DAILY_MESSAGE_PATH))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8));

        enviarRequisicao(builder);
    }

    // ============================================================
    // VERIFICAÇÃO DE CONEXÃO
    // ============================================================

    public String checkServerStatus() {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpoint()))
                    .timeout(Duration.ofSeconds(3))
                    .GET();

            HttpResponse<String> response = client.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return "online";
            } else {
                return "offline-" + response.statusCode();
            }
        } catch (Exception e) {
            return "offline-" + e.getMessage();
        }
    }

    // ============================================================
    // REQUISIÇÃO
    // ============================================================

    private HttpResponse<String> enviarRequisicao(HttpRequest.Builder builder)
            throws IOException, InterruptedException {
        adicionarTokenSeConfigurado(builder);
        HttpResponse<String> response = client.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Intranet respondeu HTTP " + response.statusCode() + ": " + response.body());
        }
        return response;
    }

    // ============================================================
    // JSON / ESTATÍSTICAS
    // ============================================================

    private static int extrairInteiro(Pattern padrao, String json) throws IOException {
        Matcher matcher = padrao.matcher(json);
        if (!matcher.find()) {
            throw new IOException("Resposta de /api/visitas em formato inesperado: " + json);
        }
        return Integer.parseInt(matcher.group(1));
    }

    private static String extrairTexto(Pattern padrao, String json) {
        Matcher matcher = padrao.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static void adicionarTokenSeConfigurado(HttpRequest.Builder builder) {
        String token = System.getenv("INTRANET_AVISOS_TOKEN");
        if (token != null && !token.isBlank()) {
            builder.header("x-intranet-token", token);
        }
    }

    // ============================================================
    // IMAGEM
    // ============================================================

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

    // ============================================================
    // ENDPOINTS
    // ============================================================

    public static String endpoint() {
        String configured = System.getProperty("intranet.avisos.url", System.getenv("INTRANET_AVISOS_URL"));
        return configured == null || configured.isBlank() ? baseUrl() + "/api/avisos" : validarUrl(configured);
    }

    public static String presenceEndpoint() {
        String configured = System.getProperty("intranet.presence.url", System.getenv("INTRANET_PRESENCE_URL"));
        return configured == null || configured.isBlank() ? baseUrl() + "/api/visitas" : validarUrl(configured);
    }

    // ============================================================
    // BASE URL
    // ============================================================

    public static String baseUrl() {
        String configured = System.getProperty("intranet.base.url");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("INTRANET_BASE_URL");
        }
        if (configured == null || configured.isBlank()) {
            configured = PREFERENCES.get(BASE_URL_PREFERENCE, null);
        }
        String value = configured == null || configured.isBlank() ? DEFAULT_BASE_URL : configured;
        return validarUrl(value).replaceFirst("/+$", "");
    }

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
        boolean http = "http".equalsIgnoreCase(uri.getScheme());
        boolean https = "https".equalsIgnoreCase(uri.getScheme());
        if ((!http && !https) || uri.getHost() == null) {
            throw new IllegalArgumentException("Use uma URL HTTP ou HTTPS válida.");
        }
        return uri.toString();
    }

    // ============================================================
    // PARSING - Métodos auxiliares
    // ============================================================

    private static String extrairCampoTexto(String json, String campo) {
        if (json == null || campo == null) return null;

        try {
            Pattern pattern = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return unescapeJson(matcher.group(1));
            }

            Pattern pattern2 = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*([^,}\\]]+)");
            Matcher matcher2 = pattern2.matcher(json);
            if (matcher2.find()) {
                String value = matcher2.group(1).trim();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    return unescapeJson(value.substring(1, value.length() - 1));
                }
                return value;
            }
        } catch (Exception e) {
            System.err.println("Erro ao extrair campo '" + campo + "': " + e.getMessage());
        }
        return null;
    }

    private static String extrairCampoNumero(String json, String campo) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static boolean extrairCampoBoolean(String json, String campo, boolean padrao) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : padrao;
    }

    private static String unescapeJson(String valor) {
        return valor
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static void appendJsonField(StringBuilder json, String key, String value, boolean primeiroCampo) {
        if (!primeiroCampo) json.append(',');
        json.append('"').append(escape(key)).append("\":");
        if (value == null) {
            json.append("null");
        } else {
            json.append('"').append(escape(value)).append('"');
        }
    }

    private static void appendJsonBooleanField(StringBuilder json, String key, boolean value, boolean primeiroCampo) {
        if (!primeiroCampo) json.append(',');
        json.append('"').append(escape(key)).append("\":").append(value);
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

    // ============================================================
    // MÉTODO PARA TESTES - DADOS MOCK
    // ============================================================

    public List<Popup> listarPopupsMock() {
        List<Popup> mock = new ArrayList<>();
        mock.add(new Popup(
                "1",
                "Manutenção Programada",
                "Sistema ficará indisponível das 22h às 23h para manutenção programada.",
                true,
                "Problema",
                "Médio",
                "Central, Login"
        ));
        mock.add(new Popup(
                "2",
                "Novo Sistema de RH",
                "O novo sistema de RH está disponível para todos os colaboradores.",
                false,
                "Informativo",
                "Grande",
                "Central"
        ));
        mock.add(new Popup(
                "3",
                "Alerta de Segurança",
                "Atualize sua senha a cada 90 dias para manter a segurança.",
                true,
                "Alerta",
                "Pequeno",
                "Login"
        ));
        return mock;
    }
}