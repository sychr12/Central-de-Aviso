package com.example.intranet_adm.service;

import com.example.intranet_adm.model.Popup;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Serviço responsável pelo gerenciamento dos popups
 * publicados na Intranet.
 *
 * Centraliza as operações de:
 * - listar;
 * - ativar;
 * - desativar;
 * - alterar status;
 * - excluir;
 * - apagar popups.
 */
public class PopupService {

    private final IntranetAvisosClient client;

    // ============================================================
    // CONSTRUTORES
    // ============================================================

    /**
     * Construtor padrão.
     */
    public PopupService() {
        this(new IntranetAvisosClient());
    }

    /**
     * Construtor utilizando um cliente existente.
     *
     * Permite compartilhar a mesma configuração de conexão
     * utilizada pela aplicação.
     */
    public PopupService(
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
    // LISTAR
    // ============================================================

    /**
     * Lista todos os popups cadastrados no servidor.
     */
    public List<Popup> listar()
            throws IOException, InterruptedException {

        List<Popup> popups =
                client.listarPopups();

        if (popups == null) {
            return Collections.emptyList();
        }

        return popups;
    }

    /**
     * Alias para listar().
     */
    public List<Popup> listarPopups()
            throws IOException, InterruptedException {

        return listar();
    }

    // ============================================================
    // ATIVAR
    // ============================================================

    /**
     * Ativa um popup.
     */
    public void ativar(
            String id
    ) throws IOException, InterruptedException {

        validarId(id);

        client.ativarPopup(id);
    }

    /**
     * Alias para ativar().
     */
    public void ativarPopup(
            String id
    ) throws IOException, InterruptedException {

        ativar(id);
    }

    // ============================================================
    // DESATIVAR
    // ============================================================

    /**
     * Desativa um popup.
     */
    public void desativar(
            String id
    ) throws IOException, InterruptedException {

        validarId(id);

        client.desativarPopup(id);
    }

    /**
     * Alias para desativar().
     */
    public void desativarPopup(
            String id
    ) throws IOException, InterruptedException {

        desativar(id);
    }

    // ============================================================
    // ALTERAR STATUS
    // ============================================================

    /**
     * Altera o status de um popup.
     *
     * @param id ID do popup
     * @param ativo true para ativar, false para desativar
     */
    public void alterarStatus(
            String id,
            boolean ativo
    ) throws IOException, InterruptedException {

        validarId(id);

        client.alterarStatusPopup(
                id,
                ativo
        );
    }

    /**
     * Alias para alterarStatus().
     */
    public void alterarStatusPopup(
            String id,
            boolean ativo
    ) throws IOException, InterruptedException {

        alterarStatus(
                id,
                ativo
        );
    }

    // ============================================================
    // EXCLUIR
    // ============================================================

    /**
     * Exclui um popup definitivamente.
     */
    public void excluir(
            String id
    ) throws IOException, InterruptedException {

        validarId(id);

        client.excluirPopup(id);
    }

    /**
     * Alias para excluir().
     */
    public void excluirPopup(
            String id
    ) throws IOException, InterruptedException {

        excluir(id);
    }

    /**
     * Alias adicional utilizado pelo código antigo.
     */
    public void apagar(
            String id
    ) throws IOException, InterruptedException {

        excluir(id);
    }

    /**
     * Alias para apagar().
     */
    public void apagarPopup(
            String id
    ) throws IOException, InterruptedException {

        apagar(id);
    }

    // ============================================================
    // BUSCAR POR ID
    // ============================================================

    /**
     * Procura um popup pelo ID.
     *
     * Retorna null caso não exista.
     */
    public Popup buscarPorId(
            String id
    ) throws IOException, InterruptedException {

        validarId(id);

        List<Popup> popups =
                listar();

        for (
                Popup popup :
                popups
        ) {

            if (
                    popup != null &&
                            id.equals(popup.getId())
            ) {

                return popup;
            }
        }

        return null;
    }

    // ============================================================
    // VERIFICAÇÃO
    // ============================================================

    /**
     * Verifica se um popup existe.
     */
    public boolean existe(
            String id
    ) throws IOException, InterruptedException {

        return buscarPorId(id) != null;
    }

    // ============================================================
    // CLIENT
    // ============================================================

    /**
     * Retorna o cliente utilizado pelo serviço.
     */
    public IntranetAvisosClient getClient() {

        return client;
    }

    // ============================================================
    // VALIDAÇÃO
    // ============================================================

    private static void validarId(
            String id
    ) {

        if (
                id == null ||
                        id.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "ID do popup é obrigatório."
            );
        }
    }
}