package com.example.intranet_adm.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gerencia as mensagens do dia armazenadas localmente.
 *
 * As mensagens são mantidas em memória e persistidas
 * em um arquivo local.
 */
public final class DailyMessages {

    private static final String DEFAULT_MESSAGE =
            "Nenhuma mensagem cadastrada.";

    private static final List<String> MESSAGES =
            new ArrayList<>();

    private DailyMessages() {
        // Classe utilitária.
    }

    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    static {
        carregarDoArquivo();
    }

    // ============================================================
    // CARREGAR
    // ============================================================

    private static synchronized void carregarDoArquivo() {

        Path arquivo = getArquivoMensagens();

        if (!Files.exists(arquivo)) {
            return;
        }

        try {

            List<String> linhas =
                    Files.readAllLines(
                            arquivo,
                            StandardCharsets.UTF_8
                    );

            MESSAGES.clear();

            for (String linha : linhas) {

                if (linha == null) {
                    continue;
                }

                String mensagem =
                        linha.trim();

                if (!mensagem.isEmpty()
                        && !MESSAGES.contains(mensagem)) {

                    MESSAGES.add(mensagem);
                }
            }

        } catch (IOException error) {

            System.err.println(
                    "Não foi possível carregar as mensagens: "
                            + error.getMessage()
            );

            MESSAGES.clear();
        }
    }

    // ============================================================
    // SALVAR
    // ============================================================

    private static synchronized boolean salvarNoArquivo() {

        Path arquivo =
                getArquivoMensagens();

        try {

            Path diretorio =
                    arquivo.getParent();

            if (diretorio != null) {

                Files.createDirectories(
                        diretorio
                );
            }

            Files.write(
                    arquivo,
                    MESSAGES,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            return true;

        } catch (IOException error) {

            System.err.println(
                    "Não foi possível salvar as mensagens: "
                            + error.getMessage()
            );

            return false;
        }
    }

    // ============================================================
    // LOCAL DO ARQUIVO
    // ============================================================

    private static Path getArquivoMensagens() {

        String appData =
                System.getenv("APPDATA");

        Path baseDirectory;

        if (appData == null
                || appData.isBlank()) {

            baseDirectory =
                    Path.of(
                            System.getProperty(
                                    "user.home"
                            ),
                            ".intranet-adm"
                    );

        } else {

            baseDirectory =
                    Path.of(
                            appData,
                            "Intranet-IDAM"
                    );
        }

        return baseDirectory.resolve(
                "daily-messages.json"
        );
    }

    // ============================================================
    // LISTAR MENSAGENS
    // ============================================================

    public static synchronized List<String> getMessages() {

        return Collections.unmodifiableList(
                new ArrayList<>(MESSAGES)
        );
    }

    // ============================================================
    // ADICIONAR
    // ============================================================

    public static synchronized void addMessage(
            String message
    ) {

        if (message == null) {
            return;
        }

        String cleanMessage =
                message.trim();

        if (cleanMessage.isEmpty()) {
            return;
        }

        if (MESSAGES.contains(cleanMessage)) {
            return;
        }

        MESSAGES.add(
                cleanMessage
        );

        if (!salvarNoArquivo()) {

            // Reverte a alteração caso não consiga salvar.
            MESSAGES.remove(
                    cleanMessage
            );
        }
    }

    // ============================================================
    // REMOVER
    // ============================================================

    public static synchronized boolean removeMessage(
            String message
    ) {

        if (message == null
                || message.isBlank()) {

            return false;
        }

        String mensagem =
                message.trim();

        int indiceEncontrado = -1;

        for (
                int i = 0;
                i < MESSAGES.size();
                i++
        ) {

            String atual =
                    MESSAGES.get(i);

            if (
                    atual != null
                            && atual.trim()
                            .equals(mensagem)
            ) {

                indiceEncontrado = i;
                break;
            }
        }

        if (indiceEncontrado == -1) {
            return false;
        }

        String removida =
                MESSAGES.remove(
                        indiceEncontrado
                );

        if (!salvarNoArquivo()) {

            // Restaura a mensagem se o arquivo não puder ser salvo.
            MESSAGES.add(
                    indiceEncontrado,
                    removida
            );

            return false;
        }

        return true;
    }

    // ============================================================
    // LIMPAR
    // ============================================================

    public static synchronized void clearMessages() {

        List<String> backup =
                new ArrayList<>(
                        MESSAGES
                );

        MESSAGES.clear();

        if (!salvarNoArquivo()) {

            MESSAGES.addAll(
                    backup
            );
        }
    }

    // ============================================================
    // QUANTIDADE
    // ============================================================

    public static synchronized int getMessageCount() {

        return MESSAGES.size();
    }

    // ============================================================
    // MENSAGEM DO DIA
    // ============================================================

    public static synchronized String getMessageOfTheDay() {

        if (MESSAGES.isEmpty()) {

            return DEFAULT_MESSAGE;
        }

        LocalDate hoje =
                LocalDate.now();


        int indice =
                (hoje.getYear()
                        + hoje.getDayOfYear())
                        % MESSAGES.size();

        return MESSAGES.get(
                indice
        );
    }
}