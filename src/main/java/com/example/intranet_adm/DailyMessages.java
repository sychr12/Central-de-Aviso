package com.example.intranet_adm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DailyMessages {

    private DailyMessages() {
        // Impede criar objetos dessa classe
    }

    /*
     * TODAS AS MENSAGENS DO SISTEMA FICAM AQUI.
     *
     * Para adicionar uma nova mensagem:
     *
     * "Minha nova mensagem aqui.",
     *
     * Não precisa alterar nenhuma outra classe.
     */
    private static final List<String> MESSAGES = new ArrayList<>(List.of(

            "Um bom dia começa com gentileza, presença e disposição para fazer a diferença.",

            "Pequenos avanços, quando constantes, criam resultados que realmente importam.",

            "Compartilhar conhecimento torna o trabalho de toda a equipe mais forte.",

            "Faça o que está ao seu alcance hoje; o próximo passo fica mais claro depois.",

            "Escutar com atenção é uma das formas mais simples de construir boas soluções.",

            "Organização e colaboração transformam desafios em oportunidades de aprendizado.",

            "Reconheça cada conquista: elas ajudam a sustentar a motivação do caminho.",

            "Trabalhar com propósito é encontrar sentido também nas tarefas mais simples.",

            "Uma atitude respeitosa pode melhorar o dia de quem está ao seu redor.",

            "A criatividade aparece quando damos espaço para perguntas e novas ideias.",

            "Cuidar das relações também faz parte de construir bons resultados.",

            "Hoje é uma nova oportunidade para aprender, colaborar e evoluir.",

            "Planeje com calma, execute com atenção e celebre o que foi bem feito.",

            "O serviço público ganha força quando cada pessoa contribui com responsabilidade.",

            "Resiliência é continuar trabalhando com foco mesmo quando os resultados demoram a aparecer.",

            "A transparência nas ações cria mais confiança e mais eficiência no trabalho coletivo.",

            "Peça ajuda sempre que precisar; a colaboração torna o trabalho mais leve e produtivo.",

            "Valorize a diversidade de opiniões, pois elas ajudam a encontrar soluções mais completas.",

            "Permita-se pausar e reavaliar prioridades para trabalhar com mais clareza.",

            "O cuidado com os detalhes faz a diferença em cada serviço prestado à sociedade.",

            "Cada desafio é uma oportunidade para desenvolver novas habilidades.",

            "O trabalho em equipe transforma boas ideias em grandes resultados.",

            "Comece o dia com foco, organização e disposição para aprender.",

            "Uma boa comunicação evita problemas e fortalece a equipe.",

            "Valorize o trabalho dos seus colegas e contribua sempre que puder.",

            "Grandes resultados são construídos através de pequenas atitudes.",

            "Aprender algo novo todos os dias é uma forma de continuar evoluindo.",

            "Quando trabalhamos juntos, encontramos soluções melhores.",

            "Dedicação e responsabilidade fazem parte de um trabalho bem realizado.",

            "Um ambiente positivo começa com as atitudes de cada pessoa."

    ));

    /**
     * Retorna todas as mensagens cadastradas.
     */
    public static List<String> getMessages() {
        return Collections.unmodifiableList(MESSAGES);
    }

    /**
     * Adiciona uma nova mensagem.
     */
    public static void addMessage(String message) {

        if (message == null) {
            return;
        }

        String cleanMessage = message.trim();

        if (cleanMessage.isEmpty()) {
            return;
        }

        MESSAGES.add(cleanMessage);
    }

    /**
     * Remove uma mensagem existente.
     */
    public static boolean removeMessage(String message) {

        if (message == null || message.isBlank()) {
            return false;
        }

        return MESSAGES.remove(message.trim());
    }

    /**
     * Retorna a quantidade de mensagens cadastradas.
     */
    public static int getMessageCount() {
        return MESSAGES.size();
    }

    /**
     * Retorna a mensagem correspondente ao dia atual.
     *
     * A mesma data sempre retorna a mesma mensagem.
     */
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