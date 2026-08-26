package com.example.intranet_adm.service;

import com.example.intranet_adm.model.EstatisticasAcesso;

import java.io.IOException;

/**
 * Serviço responsável pelo monitoramento dos acessos
 * à Intranet.
 *
 * Este serviço utiliza o IntranetAvisosClient para
 * consultar as estatísticas disponibilizadas pelo
 * servidor.
 */
public class MonitorService {

    private final IntranetAvisosClient client;

    // ============================================================
    // CONSTRUTORES
    // ============================================================

    /**
     * Construtor padrão.
     *
     * Cria automaticamente um cliente para comunicação
     * com o servidor da Intranet.
     */
    public MonitorService() {
        this(new IntranetAvisosClient());
    }

    /**
     * Construtor permitindo reutilizar um cliente existente.
     *
     * Isso é útil para manter a mesma configuração de conexão
     * utilizada pelas outras Views da aplicação.
     */
    public MonitorService(
            IntranetAvisosClient client
    ) {

        if (client == null) {
            throw new IllegalArgumentException(
                    "O cliente da Intranet não pode ser nulo."
            );
        }

        this.client = client;
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    /**
     * Busca as estatísticas atuais de acesso.
     */
    public EstatisticasAcesso
    buscarEstatisticas()
            throws IOException, InterruptedException {

        return client.buscarEstatisticasAcesso();
    }

    // ============================================================
    // ONLINE AGORA
    // ============================================================

    /**
     * Retorna a quantidade de usuários online atualmente.
     */
    public int buscarOnlineAgora()
            throws IOException, InterruptedException {

        return buscarEstatisticas()
                .getOnlineAgora();
    }

    // ============================================================
    // ACESSOS HOJE
    // ============================================================

    /**
     * Retorna a quantidade de acessos registrados hoje.
     */
    public int buscarAcessosHoje()
            throws IOException, InterruptedException {

        return buscarEstatisticas()
                .getAcessosHoje();
    }

    // ============================================================
    // TOTAL DE VISITANTES
    // ============================================================

    /**
     * Retorna o total de visitantes registrados.
     */
    public int buscarTotalVisitantes()
            throws IOException, InterruptedException {

        return buscarEstatisticas()
                .getTotalVisitantes();
    }

    // ============================================================
    // ÚLTIMA CONEXÃO
    // ============================================================

    /**
     * Retorna a data/hora da última conexão registrada.
     *
     * Pode retornar null caso o servidor não forneça
     * essa informação.
     */
    public String buscarUltimaConexao()
            throws IOException, InterruptedException {

        return buscarEstatisticas()
                .getUltimaConexao();
    }

    // ============================================================
    // STATUS DO SERVIDOR
    // ============================================================

    /**
     * Verifica se o servidor da Intranet está acessível.
     *
     * Retorna:
     * - "online" quando o servidor responde;
     * - "offline-..." quando ocorre algum problema.
     */
    public String verificarStatusServidor() {

        return client.checkServerStatus();
    }

    /**
     * Retorna true quando o servidor está online.
     */
    public boolean servidorOnline() {

        return "online".equalsIgnoreCase(
                verificarStatusServidor()
        );
    }

    // ============================================================
    // CLIENTE
    // ============================================================

    /**
     * Retorna o cliente utilizado pelo serviço.
     *
     * Útil caso alguma View precise realizar uma operação
     * adicional através do mesmo cliente.
     */
    public IntranetAvisosClient getClient() {

        return client;
    }
}