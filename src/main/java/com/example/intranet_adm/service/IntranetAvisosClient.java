package com.example.intranet_adm.service;

import com.example.intranet_adm.model.EstatisticasAcesso;
import com.example.intranet_adm.model.Popup;

import java.io.IOException;
import java.net.URI;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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

    private static final String DEFAULT_BASE_URL =
            "http://localhost:3000";

    private static final String BASE_URL_PREFERENCE =
            "intranet.base.url";

    private static final String DAILY_MESSAGE_PATH =
            "/api/daily-message";

    private static final long MAX_IMAGE_BYTES =
            5L * 1024 * 1024;

    private static final Preferences PREFERENCES =
            Preferences.userNodeForPackage(
                    IntranetAvisosClient.class
            );

    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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

    private static final Map<String, String> MIME_BY_EXTENSION =
            Map.of(
                    "jpg", "image/jpeg",
                    "jpeg", "image/jpeg",
                    "png", "image/png",
                    "gif", "image/gif",
                    "webp", "image/webp"
                    ,"pdf", "application/pdf"
                    ,"mp4", "video/mp4"
                    ,"webm", "video/webm"
                    ,"ogv", "video/ogg"
                    ,"mov", "video/quicktime"
            );

    private final HttpClient client =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .followRedirects(
                            HttpClient.Redirect.NORMAL
                    )
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

    // ============================================================
    // CONFIGURAÇÃO DO AVISO
    // ============================================================

    public static class AvisoConfig {

        private final String titulo;
        private final String mensagem;

        private String prioridade = "normal";
        private String criticidade = "informative";
        private String link;
        private Path imagem;
        private LocalDateTime publicarEm;
        private LocalDateTime expirarEm;

        private boolean ativo = true;
        private boolean podeFechar = true;

        public AvisoConfig(
                String titulo,
                String mensagem
        ) {

            if (titulo == null || titulo.isBlank()) {
                throw new IllegalArgumentException(
                        "Título é obrigatório."
                );
            }

            if (mensagem == null || mensagem.isBlank()) {
                throw new IllegalArgumentException(
                        "Mensagem é obrigatória."
                );
            }

            this.titulo = titulo.trim();
            this.mensagem = mensagem.trim();
        }

        public AvisoConfig comPrioridade(
                String prioridade
        ) {

            if (prioridade != null &&
                    !prioridade.isBlank()) {

                this.prioridade =
                        prioridade.trim();
            }

            return this;
        }

        public AvisoConfig comLink(String link) {

            this.link =
                    link == null || link.isBlank()
                            ? null
                            : link.trim();

            return this;
        }

        public AvisoConfig comCriticidade(String criticidade) {
            if (criticidade != null && !criticidade.isBlank()) {
                this.criticidade = criticidade.trim();
            }
            return this;
        }

        public AvisoConfig comImagem(Path imagem) {

            this.imagem = imagem;

            return this;
        }

        public AvisoConfig comPublicarEm(
                LocalDateTime publicarEm
        ) {

            this.publicarEm = publicarEm;

            return this;
        }

        public AvisoConfig comExpirarEm(
                LocalDateTime expirarEm
        ) {

            this.expirarEm = expirarEm;

            return this;
        }

        public AvisoConfig comAtivo(
                boolean ativo
        ) {

            this.ativo = ativo;

            return this;
        }

        public AvisoConfig comPodeFechar(
                boolean podeFechar
        ) {

            this.podeFechar = podeFechar;

            return this;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getMensagem() {
            return mensagem;
        }

        public String getPrioridade() {
            return prioridade;
        }

        public String getLink() {
            return link;
        }

        public String getCriticidade() {
            return criticidade;
        }

        public Path getImagem() {
            return imagem;
        }

        public LocalDateTime getPublicarEm() {
            return publicarEm;
        }

        public LocalDateTime getExpirarEm() {
            return expirarEm;
        }

        public boolean isAtivo() {
            return ativo;
        }

        public boolean isPodeFechar() {
            return podeFechar;
        }
    }

    // ============================================================
    // ENVIAR AVISO
    // ============================================================

    public void enviar(
            AvisoConfig config
    ) throws IOException, InterruptedException {

        if (config == null) {
            throw new IllegalArgumentException(
                    "Configuração do aviso não pode ser nula."
            );
        }

        validarDatas(config);

        StringBuilder json =
                new StringBuilder(512);

        json.append('{');

        appendJsonField(
                json,
                "title",
                config.getTitulo(),
                true
        );

        appendJsonField(
                json,
                "message",
                config.getMensagem(),
                false
        );

        appendJsonField(
                json,
                "priority",
                config.getPrioridade(),
                false
        );

        appendJsonField(
                json,
                "criticality",
                config.getCriticidade(),
                false
        );

        appendJsonField(
                json,
                "link",
                config.getLink(),
                false
        );

        if (config.getPublicarEm() != null) {

            appendJsonField(
                    json,
                    "publishedDate",
                    config.getPublicarEm()
                            .format(ISO_LOCAL_DATE_TIME),
                    false
            );
        }

        if (config.getExpirarEm() != null) {

            appendJsonField(
                    json,
                    "expirationDate",
                    config.getExpirarEm()
                            .format(ISO_LOCAL_DATE_TIME),
                    false
            );
        }

        appendJsonBooleanField(
                json,
                "active",
                config.isAtivo(),
                false
        );

        appendJsonBooleanField(
                json,
                "closable",
                config.isPodeFechar(),
                false
        );

        adicionarImagemAoJson(
                json,
                config.getImagem()
        );

        json.append('}');

        HttpRequest.Builder request =
                HttpRequest.newBuilder(
                                URI.create(endpoint())
                        )
                        .timeout(Duration.ofSeconds(20))
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

        enviarRequisicao(request);
    }

    public void enviar(
            String titulo,
            String mensagem
    ) throws IOException, InterruptedException {

        enviar(
                new AvisoConfig(
                        titulo,
                        mensagem
                )
        );
    }

    // ============================================================
    // POPUPS
    // ============================================================

    public List<Popup> listarPopups()
            throws IOException, InterruptedException {

        HttpRequest.Builder request =
                HttpRequest.newBuilder(
                                URI.create(endpoint())
                        )
                        .timeout(Duration.ofSeconds(10))
                        .GET();

        HttpResponse<String> response =
                enviarRequisicao(request);

        return parsePopups(
                response.body()
        );
    }

    public void ativarPopup(String id)
            throws IOException, InterruptedException {

        alterarStatusPopup(
                id,
                true
        );
    }

    public void desativarPopup(String id)
            throws IOException, InterruptedException {

        alterarStatusPopup(
                id,
                false
        );
    }

    public void alterarStatusPopup(
            String id,
            boolean ativo
    ) throws IOException, InterruptedException {

        validarPopupId(id);

        String json =
                "{\"active\":" +
                        ativo +
                        "}";

        String url =
                endpoint()
                        + "?id="
                        + URLEncoder.encode(
                        id,
                        StandardCharsets.UTF_8
                );

        HttpRequest.Builder request =
                HttpRequest.newBuilder(
                                URI.create(url)
                        )
                        .timeout(Duration.ofSeconds(10))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .method(
                                "PATCH",
                                HttpRequest.BodyPublishers.ofString(
                                        json,
                                        StandardCharsets.UTF_8
                                )
                        );

        enviarRequisicao(request);
    }

    public void excluirPopup(String id)
            throws IOException, InterruptedException {

        validarPopupId(id);

        String url =
                endpoint()
                        + "?id="
                        + URLEncoder.encode(
                        id,
                        StandardCharsets.UTF_8
                );

        HttpRequest.Builder request =
                HttpRequest.newBuilder(
                                URI.create(url)
                        )
                        .timeout(Duration.ofSeconds(10))
                        .DELETE();

        enviarRequisicao(request);
    }

    public void apagarPopup(String id)
            throws IOException, InterruptedException {

        excluirPopup(id);
    }

    private static void validarPopupId(
            String id
    ) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "ID do popup é obrigatório."
            );
        }
    }

    // ============================================================
    // PARSE DOS POPUPS
    // ============================================================

    private List<Popup> parsePopups(
            String json
    ) {

        List<Popup> resultado =
                new ArrayList<>();

        if (json == null ||
                json.isBlank()) {

            return resultado;
        }

        String texto =
                json.trim();

        if (texto.startsWith("{")) {

            Popup popup =
                    parsePopupObject(texto);

            if (popup != null) {
                resultado.add(popup);
            }

            return resultado;
        }

        if (texto.startsWith("[")) {

            for (String objeto :
                    extrairObjetosJson(texto)) {

                Popup popup =
                        parsePopupObject(objeto);

                if (popup != null) {
                    resultado.add(popup);
                }
            }
        }

        return resultado;
    }

    private Popup parsePopupObject(
            String objeto
    ) {

        try {

            String id =
                    extrairCampoTexto(
                            objeto,
                            "id"
                    );

            if (id == null) {
                id =
                        extrairCampoNumero(
                                objeto,
                                "id"
                        );
            }

            String titulo =
                    primeiroValor(
                            extrairCampoTexto(
                                    objeto,
                                    "title"
                            ),
                            extrairCampoTexto(
                                    objeto,
                                    "titulo"
                            ),
                            "Sem título"
                    );

            String mensagem =
                    primeiroValor(
                            extrairCampoTexto(
                                    objeto,
                                    "message"
                            ),
                            extrairCampoTexto(
                                    objeto,
                                    "mensagem"
                            ),
                            ""
                    );

            boolean ativo =
                    extrairCampoBoolean(
                            objeto,
                            "active",
                            extrairCampoBoolean(
                                    objeto,
                                    "ativo",
                                    true
                            )
                    );

            String expirationDate =
                    extrairCampoTexto(
                            objeto,
                            "expirationDate"
                    );

            if (expirationDate != null &&
                    expirada(expirationDate)) {

                ativo = false;
            }

            String modelo =
                    primeiroValor(
                            extrairCampoTexto(
                                    objeto,
                                    "model"
                            ),
                            extrairCampoTexto(
                                    objeto,
                                    "modelo"
                            ),
                            "Popup"
                    );

            String tamanho =
                    primeiroValor(
                            extrairCampoTexto(
                                    objeto,
                                    "size"
                            ),
                            extrairCampoTexto(
                                    objeto,
                                    "tamanho"
                            ),
                            "Médio"
                    );

            String paginas =
                    primeiroValor(
                            extrairCampoTexto(
                                    objeto,
                                    "pages"
                            ),
                            extrairCampoTexto(
                                    objeto,
                                    "paginas"
                            ),
                            "Nenhuma"
                    );

            // A Intranet salva os anexos em /uploads/... e devolve esse caminho
            // relativo. A Central precisa de uma URL absoluta para conseguir
            // carregar a imagem no histórico e no popup.
            String imagem = extrairCampoTexto(objeto, "imageUrl");
            if (imagem != null && imagem.startsWith("/")) {
                imagem = baseUrl().replaceFirst("/+$", "") + imagem;
            }
            if (imagem == null || imagem.isBlank()) {
                imagem = extrairCampoTexto(objeto, "imageData");
            }
            if (imagem == null) {
                String base64 = extrairCampoTexto(objeto, "imageBase64");
                String mime = extrairCampoTexto(objeto, "imageMimeType");
                if (mime == null || mime.isBlank()) mime = "image/png";
                if (base64 != null && !base64.isBlank()) imagem = "data:" + mime + ";base64," + base64;
            }

            Popup popup = new Popup(
                    id != null
                            ? id
                            : String.valueOf(
                            System.currentTimeMillis()
                    ),
                    titulo,
                    mensagem,
                    ativo,
                    modelo,
                    tamanho,
                    paginas
            );
            popup.setImagem(imagem);
            return popup;

        } catch (Exception error) {

            System.err.println(
                    "Erro ao processar popup: "
                            + error.getMessage()
            );

            return null;
        }
    }

    private List<String> extrairObjetosJson(
            String json
    ) {

        List<String> objetos =
                new ArrayList<>();

        int nivel = 0;
        int inicio = -1;

        boolean dentroString = false;
        boolean escape = false;

        for (int i = 0;
             i < json.length();
             i++) {

            char caractere =
                    json.charAt(i);

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
            }

            if (caractere == '}') {

                nivel--;

                if (nivel == 0 &&
                        inicio >= 0) {

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

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    public EstatisticasAcesso buscarEstatisticasAcesso()
            throws IOException, InterruptedException {

        HttpRequest.Builder request =
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
                enviarRequisicao(request)
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

    public List<String[]> buscarVisitantes() throws IOException, InterruptedException {
        String body = enviarRequisicao(HttpRequest.newBuilder(URI.create(presenceEndpoint())).timeout(Duration.ofSeconds(8)).GET()).body();
        List<String[]> result = new ArrayList<>();
        Pattern item = Pattern.compile("\\{[^{}]*\\\"nome\\\"[^{}]*}", Pattern.DOTALL);
        Matcher matcher = item.matcher(body);
        while (matcher.find()) {
            String visitante = matcher.group();
            result.add(new String[]{
                    campoVisitante(visitante, "nome", "Visitante"),
                    campoVisitante(visitante, "departamento", "Não informado"),
                    campoVisitante(visitante, "pagina", "/"),
                    campoVisitante(visitante, "tempo", "—"),
                    campoVisitante(visitante, "navegador", "Não informado"),
                    campoVisitante(visitante, "ip", "Não informado"),
                    campoVisitante(visitante, "maquina", "Não informado"),
                    campoVisitante(visitante, "ultimaAtividade", "")
            });
        }
        return result;
    }

    private static String campoVisitante(String json, String campo, String padrao) {
        String valor = extrairCampoTexto(json, campo);
        if (valor == null || valor.isBlank() || "null".equalsIgnoreCase(valor.trim())) {
            return padrao;
        }
        return valor;
    }

    // ============================================================
    // MENSAGEM DO DIA
    // ============================================================

    public void atualizarMensagemDoDia(
            String mensagem
    ) throws IOException, InterruptedException {

        if (mensagem == null ||
                mensagem.isBlank()) {

            throw new IllegalArgumentException(
                    "Mensagem não pode ser vazia."
            );
        }

        StringBuilder json =
                new StringBuilder();

        json.append('{');

        appendJsonField(
                json,
                "message",
                mensagem.trim(),
                true
        );

        json.append('}');

        HttpRequest.Builder request =
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

        enviarRequisicao(request);
    }

    public void removerMensagemDoDia(
            String mensagem
    ) throws IOException, InterruptedException {

        if (mensagem == null ||
                mensagem.isBlank()) {

            throw new IllegalArgumentException(
                    "Mensagem não pode ser vazia."
            );
        }

        StringBuilder json =
                new StringBuilder();

        json.append('{');

        appendJsonField(
                json,
                "message",
                mensagem.trim(),
                true
        );

        json.append('}');

        HttpRequest.Builder request =
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
                        .method(
                                "DELETE",
                                HttpRequest.BodyPublishers.ofString(
                                        json.toString(),
                                        StandardCharsets.UTF_8
                                )
                        );

        enviarRequisicao(request);
    }

    // ============================================================
    // STATUS DO SERVIDOR
    // ============================================================

    public String checkServerStatus() {
        return checkServerStatus(baseUrl());
    }

    public String checkServerStatus(String base) {

        try {

            HttpRequest.Builder request =
                    HttpRequest.newBuilder(
                                    URI.create(validarUrl(base).replaceFirst("/+$", "") + "/api/avisos")
                            )
                            .timeout(
                                    Duration.ofSeconds(3)
                            )
                            .GET();
            adicionarTokenSeConfigurado(request);

            HttpResponse<String> response =
                    client.send(
                            request.build(),
                            HttpResponse.BodyHandlers.ofString(
                                    StandardCharsets.UTF_8
                            )
                    );

            if (response.statusCode() >= 200 &&
                    response.statusCode() < 300) {

                return "online";
            }

            return "offline-" +
                    response.statusCode();

        } catch (Exception error) {

            return "offline-" +
                    error.getMessage();
        }
    }

    // ============================================================
    // HTTP
    // ============================================================

    private HttpResponse<String> enviarRequisicao(
            HttpRequest.Builder builder
    ) throws IOException, InterruptedException {

        adicionarTokenSeConfigurado(
                builder
        );

        HttpResponse<String> response =
                client.send(
                        builder.build(),
                        HttpResponse.BodyHandlers.ofString(
                                StandardCharsets.UTF_8
                        )
                );

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

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
    // ENDPOINTS
    // ============================================================

    public static String endpoint() {


        // O endpoint padrão pertence ao servidor local do Intranet-IDAM.
        // URLs antigas salvas no ambiente não devem redirecionar o envio.
        if (System.getProperty("intranet.avisos.url") == null && System.getenv("INTRANET_AVISOS_URL") == null) {
            return baseUrl() + "/api/avisos";
        }

        String configured =
                System.getProperty(
                        "intranet.avisos.url"
                );

        if (configured == null ||
                configured.isBlank()) {

            configured =
                    System.getenv(
                            "INTRANET_AVISOS_URL"
                    );
        }

        if (configured == null ||
                configured.isBlank()) {

            return baseUrl()
                    + "/api/avisos";
        }

        return validarUrl(configured);
    }

    public static String presenceEndpoint() {

        String configured =
                System.getProperty(
                        "intranet.presence.url"
                );

        if (configured == null ||
                configured.isBlank()) {

            configured =
                    System.getenv(
                            "INTRANET_PRESENCE_URL"
                    );
        }

        if (configured == null ||
                configured.isBlank()) {

            return baseUrl()
                    + "/api/visitas";
        }

        return validarUrl(configured);
    }

    // ============================================================
    // BASE URL
    // ============================================================

    public static String baseUrl() {

        String configured =
                System.getProperty(
                        "intranet.base.url"
                );

        if (configured == null ||
                configured.isBlank()) {

            configured =
                    System.getenv(
                            "INTRANET_BASE_URL"
                    );
        }

        String value =
                configured == null ||
                        configured.isBlank()
                        ? PREFERENCES.get(BASE_URL_PREFERENCE, DEFAULT_BASE_URL)
                        : configured;

        return validarUrl(value)
                .replaceFirst("/+$", "");
    }

    /** Adiciona uma frase ao daily-message.json através da API da Intranet. */
    public void adicionarMensagemDoDia(String mensagem) throws IOException, InterruptedException {
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("A mensagem do dia é obrigatória.");
        }
        String escaped = mensagem.trim().replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl() + DAILY_MESSAGE_PATH))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"message\":\"" + escaped + "\"}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("API recusou a mensagem (HTTP " + response.statusCode() + ")");
        }
    }

    public List<String> listarMensagensDoDia() throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(
                        URI.create(baseUrl() + DAILY_MESSAGE_PATH))
                .timeout(Duration.ofSeconds(15))
                .GET();
        HttpResponse<String> response = enviarRequisicao(request);
        Matcher matcher = Pattern.compile("\\\"messages\\\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(response.body());
        List<String> result = new ArrayList<>();
        if (!matcher.find()) return result;
        Matcher item = Pattern.compile("\\\"((?:\\\\.|[^\\\"])*)\\\"").matcher(matcher.group(1));
        while (item.find()) result.add(unescapeJson(item.group(1)));
        return result;
    }

    public void excluirMensagemDoDia(String mensagem) throws IOException, InterruptedException { alterarMensagemDoDia("DELETE", mensagem, null); }
    public void editarMensagemDoDia(String antiga, String nova) throws IOException, InterruptedException { alterarMensagemDoDia("PUT", antiga, nova); }

    private void alterarMensagemDoDia(String metodo, String antiga, String nova) throws IOException, InterruptedException {
        String a = jsonEscape(antiga), n = nova == null ? "" : jsonEscape(nova);
        String body = metodo.equals("DELETE") ? "{\"message\":\"" + a + "\"}" : "{\"oldMessage\":\"" + a + "\",\"message\":\"" + n + "\"}";
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl() + DAILY_MESSAGE_PATH)).header("Content-Type", "application/json");
        HttpRequest request = builder.method(metodo, HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) throw new IOException("Falha ao alterar mensagem (HTTP " + response.statusCode() + ")");
    }

    private static String jsonEscape(String value) { return value.trim().replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n"); }

    private static String detectarServidorLocal() {
        String host = "localhost";
        try { host = InetAddress.getLocalHost().getHostAddress(); } catch (Exception ignored) { }
        for (int porta = 3005; porta >= 3000; porta--) {
            for (String endereco : new String[]{"localhost", host}) try {
                HttpRequest request = HttpRequest.newBuilder(URI.create("http://" + endereco + ":" + porta + "/api/avisos")).timeout(Duration.ofMillis(500)).GET().build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 500) return "http://" + endereco + ":" + porta;
            } catch (Exception ignored) { }
        }
        return DEFAULT_BASE_URL;
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

        if (value == null ||
                value.isBlank()) {

            throw new IllegalArgumentException(
                    "URL não pode ser vazia."
            );
        }

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

        if ((!http && !https) ||
                uri.getHost() == null) {

            throw new IllegalArgumentException(
                    "Use uma URL HTTP ou HTTPS válida."
            );
        }

        return uri.toString();
    }

    // ============================================================
    // IMAGEM
    // ============================================================

    private static void adicionarImagemAoJson(
            StringBuilder json,
            Path imagem
    ) throws IOException {

        if (imagem == null) {
            return;
        }

        if (!Files.exists(imagem)) {

            throw new IOException(
                    "A imagem selecionada não existe."
            );
        }

        long tamanho =
                Files.size(imagem);

        if (tamanho > MAX_IMAGE_BYTES) {

            throw new IOException(
                    "Imagem excede o tamanho máximo de 5 MB."
            );
        }

        String mime =
                mimeTypeDe(imagem);

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
                mime,
                false
        );
    }

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
                            + ". Use JPG, PNG, GIF ou WEBP."
            );
        }

        return mime;
    }

    // ============================================================
    // VALIDAÇÕES
    // ============================================================

    private static void validarDatas(
            AvisoConfig config
    ) {

        if (config.getPublicarEm() != null &&
                config.getExpirarEm() != null &&
                !config.getExpirarEm()
                        .isAfter(
                                config.getPublicarEm()
                        )) {

            throw new IllegalArgumentException(
                    "A expiração deve ser depois da publicação."
            );
        }
    }

    private static boolean expirada(
            String data
    ) {

        try {

            return Instant.now()
                    .isAfter(
                            Instant.parse(data)
                    );

        } catch (Exception ignored) {
        }

        try {

            return Instant.now()
                    .isAfter(
                            OffsetDateTime
                                    .parse(data)
                                    .toInstant()
                    );

        } catch (Exception ignored) {
        }

        try {

            return LocalDateTime.now()
                    .isAfter(
                            LocalDateTime.parse(data)
                    );

        } catch (Exception ignored) {
            return false;
        }
    }

    // ============================================================
    // JSON
    // ============================================================

    private static String extrairCampoTexto(
            String json,
            String campo
    ) {

        if (json == null ||
                campo == null) {

            return null;
        }

        try {

            Pattern pattern =
                    Pattern.compile(
                            "\""
                                    + Pattern.quote(campo)
                                    + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
                    );

            Matcher matcher =
                    pattern.matcher(json);

            if (matcher.find()) {

                return unescapeJson(
                        matcher.group(1)
                );
            }

            Pattern pattern2 =
                    Pattern.compile(
                            "\""
                                    + Pattern.quote(campo)
                                    + "\"\\s*:\\s*([^,}\\]]+)"
                    );

            Matcher matcher2 =
                    pattern2.matcher(json);

            if (matcher2.find()) {

                String value =
                        matcher2.group(1)
                                .trim();

                if (value.startsWith("\"") &&
                        value.endsWith("\"")) {

                    return unescapeJson(
                            value.substring(
                                    1,
                                    value.length() - 1
                            )
                    );
                }

                return value;
            }

        } catch (Exception error) {

            System.err.println(
                    "Erro ao extrair campo "
                            + campo
                            + ": "
                            + error.getMessage()
            );
        }

        return null;
    }

    private static String extrairCampoNumero(
            String json,
            String campo
    ) {

        Pattern pattern =
                Pattern.compile(
                        "\""
                                + Pattern.quote(campo)
                                + "\"\\s*:\\s*(-?\\d+)"
                );

        Matcher matcher =
                pattern.matcher(json);

        return matcher.find()
                ? matcher.group(1)
                : null;
    }

    private static boolean extrairCampoBoolean(
            String json,
            String campo,
            boolean padrao
    ) {

        Pattern pattern =
                Pattern.compile(
                        "\""
                                + Pattern.quote(campo)
                                + "\"\\s*:\\s*(true|false)",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(json);

        return matcher.find()
                ? Boolean.parseBoolean(
                matcher.group(1)
        )
                : padrao;
    }

    private static int extrairInteiro(
            Pattern pattern,
            String json
    ) throws IOException {

        Matcher matcher =
                pattern.matcher(json);

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
            Pattern pattern,
            String json
    ) {

        Matcher matcher =
                pattern.matcher(json);

        return matcher.find()
                ? matcher.group(1)
                : null;
    }

    private static String unescapeJson(
            String valor
    ) {

        return valor
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

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
                .append(escape(key))
                .append("\":");

        if (value == null) {

            json.append("null");

        } else {

            json.append('"')
                    .append(escape(value))
                    .append('"');
        }
    }

    private static void appendJsonBooleanField(
            StringBuilder json,
            String key,
            boolean value,
            boolean primeiroCampo
    ) {

        if (!primeiroCampo) {
            json.append(',');
        }

        json.append('"')
                .append(escape(key))
                .append("\":")
                .append(value);
    }

    private static String escape(
            String value
    ) {

        StringBuilder result =
                new StringBuilder(
                        value.length() + 16
                );

        for (int i = 0;
             i < value.length();
             i++) {

            char character =
                    value.charAt(i);

            switch (character) {

                case '"' ->
                        result.append("\\\"");

                case '\\' ->
                        result.append("\\\\");

                case '\b' ->
                        result.append("\\b");

                case '\f' ->
                        result.append("\\f");

                case '\n' ->
                        result.append("\\n");

                case '\r' ->
                        result.append("\\r");

                case '\t' ->
                        result.append("\\t");

                default -> {

                    if (character < 0x20) {

                        result.append(
                                String.format(
                                        "\\u%04x",
                                        (int) character
                                )
                        );

                    } else {

                        result.append(
                                character
                        );
                    }
                }
            }
        }

        return result.toString();
    }

    // ============================================================
    // TOKEN
    // ============================================================

    private static void adicionarTokenSeConfigurado(
            HttpRequest.Builder builder
    ) {

        String token =
                System.getenv(
                        "INTRANET_AVISOS_TOKEN"
                );

        if (token != null &&
                !token.isBlank()) {

            builder.header(
                    "x-intranet-token",
                    token
            );
        }
    }

    // ============================================================
    // UTILITÁRIOS
    // ============================================================

    private static String primeiroValor(
            String primeiro,
            String segundo,
            String padrao
    ) {

        if (primeiro != null &&
                !primeiro.isBlank()) {

            return primeiro;
        }

        if (segundo != null &&
                !segundo.isBlank()) {

            return segundo;
        }

        return padrao;
    }

    // ============================================================
    // MOCK
    // ============================================================

    public List<Popup> listarPopupsMock() {

        List<Popup> mock =
                new ArrayList<>();

        mock.add(
                new Popup(
                        "1",
                        "Manutenção Programada",
                        "Sistema ficará indisponível das 22h às 23h.",
                        true,
                        "Problema",
                        "Médio",
                        "Central, Login"
                )
        );

        mock.add(
                new Popup(
                        "2",
                        "Novo Sistema de RH",
                        "O novo sistema de RH está disponível.",
                        false,
                        "Informativo",
                        "Grande",
                        "Central"
                )
        );

        mock.add(
                new Popup(
                        "3",
                        "Alerta de Segurança",
                        "Atualize sua senha periodicamente.",
                        true,
                        "Alerta",
                        "Pequeno",
                        "Login"
                )
        );

        return mock;
    }
}
