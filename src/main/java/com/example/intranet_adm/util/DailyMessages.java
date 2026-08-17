/// Olá! Este arquivo gerencia as mensagens do dia armazenadas localmente.
/// Ele controla a leitura, adição e remoção das mensagens.
/// A MensagemDoDiaView utiliza este arquivo para atualizar a lista exibida.
/// Alterações no formato ou armazenamento das mensagens devem ser refletidas aqui. =)


package com.example.intranet_adm.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DailyMessages {

    private DailyMessages() {}

    private static final List<String> MESSAGES = new ArrayList<>();

    static {
        carregarDoArquivo();
    }

    private static void carregarDoArquivo() {
        try {
            Path arquivo = getArquivoMensagens();

            if (!Files.exists(arquivo)) {
                return;
            }

            List<String> linhas =
                    Files.readAllLines(arquivo, StandardCharsets.UTF_8);

            MESSAGES.clear();

            for (String linha : linhas) {
                String mensagem = linha.trim();

                if (!mensagem.isEmpty()) {
                    MESSAGES.add(mensagem);
                }
            }

            System.out.println("Mensagens carregadas: " + MESSAGES.size());
            System.out.println("Arquivo: " + arquivo.toAbsolutePath());

        } catch (IOException e) {
            System.err.println(
                    "Não foi possível carregar mensagens: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Salva EXATAMENTE o conteúdo atual da lista.
     */
    private static void salvarNoArquivo() {
        try {
            Path arquivo = getArquivoMensagens();

            Path diretorio = arquivo.getParent();

            if (diretorio != null) {
                Files.createDirectories(diretorio);
            }

            Files.write(
                    arquivo,
                    MESSAGES,
                    StandardCharsets.UTF_8
            );

            System.out.println("ARQUIVO SALVO COM SUCESSO:");
            System.out.println(arquivo.toAbsolutePath());
            System.out.println("CONTEUDO:");
            System.out.println(Files.readAllLines(
                    arquivo,
                    StandardCharsets.UTF_8
            ));

        } catch (IOException e) {
            System.err.println("ERRO AO SALVAR:");
            e.printStackTrace();
        }
    }

    private static Path getArquivoMensagens() {

        String appData = System.getenv("APPDATA");

        Path baseDirectory;

        if (appData == null || appData.isBlank()) {
            baseDirectory = Path.of(
                    System.getProperty("user.home"),
                    ".intranet-adm"
            );
        } else {
            baseDirectory = Path.of(
                    appData,
                    "Intranet-IDAM"
            );
        }

        return baseDirectory.resolve("daily-messages.json");
    }

    public static List<String> getMessages() {
        return Collections.unmodifiableList(
                new ArrayList<>(MESSAGES)
        );
    }

    public static void addMessage(String message) {

        if (message == null) {
            return;
        }

        String cleanMessage = message.trim();

        if (cleanMessage.isEmpty()) {
            return;
        }

        if (MESSAGES.contains(cleanMessage)) {
            return;
        }

        MESSAGES.add(cleanMessage);

        salvarNoArquivo();
    }

    /**
     * Remove a mensagem e sobrescreve o JSON.
     */
    public static boolean removeMessage(String message) {

        if (message == null || message.isBlank()) {
            return false;
        }

        String mensagem = message.trim();

        System.out.println("=================================");
        System.out.println("REMOVENDO MENSAGEM");
        System.out.println("Mensagem: [" + mensagem + "]");
        System.out.println("Antes: " + MESSAGES);

        boolean removido = MESSAGES.removeIf(
                item -> item != null
                        && item.trim().equals(mensagem)
        );

        System.out.println("Removido: " + removido);
        System.out.println("Depois: " + MESSAGES);

        if (!removido) {
            System.out.println("Mensagem não encontrada.");
            return false;
        }

        System.out.println("ARQUIVO QUE SERÁ ALTERADO:");
        System.out.println(getArquivoMensagens().toAbsolutePath());
        salvarNoArquivo();

        // Confirma o conteúdo realmente escrito no disco
        try {
            Path arquivo = getArquivoMensagens();

            List<String> arquivoDepois =
                    Files.readAllLines(
                            arquivo,
                            StandardCharsets.UTF_8
                    );

            System.out.println("CONFIRMAÇÃO DO ARQUIVO:");
            System.out.println(arquivoDepois);

        } catch (IOException e) {
            System.err.println(
                    "Erro ao verificar arquivo após exclusão: "
                            + e.getMessage()
            );
        }

        System.out.println("=================================");

        return true;
    }

    public static void clearMessages() {

        MESSAGES.clear();

        salvarNoArquivo();
    }

    public static int getMessageCount() {
        return MESSAGES.size();
    }

    public static String getMessageOfTheDay() {

        if (MESSAGES.isEmpty()) {
            return "Nenhuma mensagem cadastrada.";
        }

        LocalDate today = LocalDate.now();

        String dateKey =
                today.getYear()
                        + "-"
                        + today.getMonthValue()
                        + "-"
                        + today.getDayOfMonth();

        int total = 0;

        for (char character : dateKey.toCharArray()) {
            total += character;
        }

        int index = total % MESSAGES.size();

        return MESSAGES.get(index);
    }
}