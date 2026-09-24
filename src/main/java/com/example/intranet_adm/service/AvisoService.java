package com.example.intranet_adm.service;

import com.example.intranet_adm.model.Aviso;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Serviço responsável pelo gerenciamento do histórico de avisos.
 *
 * Responsabilidades:
 * - Adicionar avisos;
 * - Listar avisos;
 * - Remover avisos;
 * - Persistir o histórico localmente.
 */
public class AvisoService {

    private static final int LIMITE_AVISOS = 10_000;
    private static final int STORAGE_MAGIC = 0x43415632;
    private static final int STORAGE_VERSION = 2;

    private static final Path DEFAULT_STORAGE =
            defaultStorage();

    private final List<Aviso> avisos =
            new ArrayList<>();

    private final Path storageFile;

    private int proximoId = 1;
    private boolean falhaCarregamento;

    // ============================================================
    // CONSTRUTORES
    // ============================================================

    /**
     * Construtor padrão.
     *
     * Utiliza o armazenamento padrão da aplicação.
     */
    public AvisoService() {
        this(DEFAULT_STORAGE);
    }

    /**
     * Construtor utilizado para definir
     * um arquivo de armazenamento específico.
     */
    public AvisoService(Path storageFile) {

        if (storageFile == null) {
            throw new IllegalArgumentException(
                    "O arquivo de armazenamento não pode ser nulo."
            );
        }

        this.storageFile = storageFile;

        carregar();
    }

    // ============================================================
    // ADICIONAR
    // ============================================================

    /**
     * Adiciona um novo aviso ao histórico.
     *
     * O aviso mais recente fica no início da lista.
     */
    public synchronized Aviso adicionar(
            String titulo,
            String mensagem,
            String autor
    ) {

        return adicionar(
                titulo,
                mensagem,
                autor,
                "Informativa",
                "Normal"
        );
    }

    public synchronized Aviso adicionar(
            String titulo,
            String mensagem,
            String autor,
            String criticidade,
            String prioridade
    ) {

        validarTexto(
                titulo,
                "O título é obrigatório."
        );

        validarTexto(
                mensagem,
                "A mensagem é obrigatória."
        );

        validarTexto(
                autor,
                "O autor é obrigatório."
        );

        Aviso aviso =
                new Aviso(
                        proximoId++,
                        titulo.trim(),
                        mensagem.trim(),
                        autor.trim(),
                        LocalDate.now(),
                        criticidade,
                        prioridade
                );

        avisos.add(
                0,
                aviso
        );

        try {

            salvar();

        } catch (IOException error) {

            avisos.remove(aviso);

            proximoId--;

            throw new IllegalStateException(
                    "Não foi possível salvar o histórico local.",
                    error
            );
        }

        return aviso;
    }

    // ============================================================
    // REMOVER
    // ============================================================

    /**
     * Remove um aviso pelo ID.
     */
    public synchronized boolean remover(
            int id
    ) {

        if (id <= 0) {
            return false;
        }

        Aviso removido = null;

        for (Aviso aviso : avisos) {

            if (aviso.getId() == id) {

                removido = aviso;

                break;
            }
        }

        if (removido == null) {
            return false;
        }

        int indiceOriginal = avisos.indexOf(removido);
        avisos.remove(removido);

        try {

            salvar();

        } catch (IOException error) {

            /*
             * Restaura o aviso caso a gravação
             * do arquivo falhe.
             */
            avisos.add(
                    0,
                    removido
            );

            throw new IllegalStateException(
                    "Não foi possível atualizar o histórico local.",
                    error
            );
        }

        return true;
    }

    // ============================================================
    // LISTAR
    // ============================================================

    /**
     * Retorna todos os avisos.
     *
     * Uma cópia da lista é retornada para impedir
     * alterações externas no armazenamento interno.
     */
    public synchronized List<Aviso> listarTodos() {

        return Collections.unmodifiableList(
                new ArrayList<>(avisos)
        );
    }

    // ============================================================
    // BUSCAR POR ID
    // ============================================================

    /**
     * Busca um aviso pelo ID.
     *
     * Retorna null caso não seja encontrado.
     */
    public synchronized Aviso buscarPorId(
            int id
    ) {

        for (Aviso aviso : avisos) {

            if (aviso.getId() == id) {
                return aviso;
            }
        }

        return null;
    }

    // ============================================================
    // QUANTIDADE
    // ============================================================

    /**
     * Retorna a quantidade de avisos armazenados.
     */
    public synchronized int quantidade() {

        return avisos.size();
    }

    // ============================================================
    // LIMPAR
    // ============================================================

    /**
     * Remove todos os avisos.
     */
    public synchronized void limpar() {

        if (avisos.isEmpty()) {
            return;
        }

        List<Aviso> backup =
                new ArrayList<>(avisos);

        avisos.clear();

        try {

            salvar();

            proximoId = 1;

        } catch (IOException error) {

            avisos.addAll(backup);

            throw new IllegalStateException(
                    "Não foi possível limpar o histórico local.",
                    error
            );
        }
    }

    // ============================================================
    // CARREGAR
    // ============================================================

    /**
     * Carrega os avisos armazenados no disco.
     */
    private synchronized void carregar() {

        avisos.clear();

        proximoId = 1;

        if (!Files.exists(storageFile)) {
            return;
        }

        try (
                DataInputStream input =
                        new DataInputStream(
                                Files.newInputStream(
                                        storageFile
                                )
                        )
        ) {

            int cabecalho = input.readInt();
            boolean formatoAtual = cabecalho == STORAGE_MAGIC;
            int total;

            if (formatoAtual) {
                int versao = input.readInt();
                if (versao != STORAGE_VERSION) {
                    throw new IOException("Versão do histórico não suportada.");
                }
                total = input.readInt();
            } else {
                total = cabecalho;
            }

            if (
                    total < 0 ||
                            total > LIMITE_AVISOS
            ) {

                throw new IOException(
                        "Quantidade de avisos inválida."
                );
            }

            for (
                    int index = 0;
                    index < total;
                    index++
            ) {

                int id =
                        input.readInt();

                String titulo =
                        input.readUTF();

                String mensagem =
                        input.readUTF();

                String autor =
                        input.readUTF();

                long epochDay =
                        input.readLong();

                LocalDate data =
                        LocalDate.ofEpochDay(
                                epochDay
                        );

                String criticidade = formatoAtual
                        ? input.readUTF()
                        : "Informativa";

                String prioridade = formatoAtual
                        ? input.readUTF()
                        : "Normal";

                Aviso aviso =
                        new Aviso(
                                id,
                                titulo,
                                mensagem,
                                autor,
                                data,
                                criticidade,
                                prioridade
                        );

                avisos.add(
                        aviso
                );

                proximoId =
                        Math.max(
                                proximoId,
                                id + 1
                        );
            }

        } catch (IOException | RuntimeException error) {
            falhaCarregamento = true;

            System.err.println(
                    "Não foi possível carregar o histórico de avisos: "
                            + error.getMessage()
            );

            avisos.clear();

            proximoId = 1;
        }
    }

    // ============================================================
    // SALVAR
    // ============================================================

    /**
     * Salva o histórico utilizando um arquivo temporário.
     *
     * Isso reduz o risco de corromper o arquivo principal
     * caso a aplicação seja encerrada durante a gravação.
     */
    private synchronized void salvar()
            throws IOException {
        if (falhaCarregamento) throw new IOException("Histórico ilegível. O arquivo original foi preservado.");

        Path diretorio =
                storageFile.getParent();

        if (diretorio != null) {

            Files.createDirectories(
                    diretorio
            );
        }

        Path temporary =
                storageFile.resolveSibling(
                        storageFile.getFileName()
                                + ".tmp"
                );

        try {

            try (
                    DataOutputStream output =
                            new DataOutputStream(
                                    Files.newOutputStream(
                                            temporary,
                                            StandardOpenOption.CREATE,
                                            StandardOpenOption.TRUNCATE_EXISTING,
                                            StandardOpenOption.WRITE
                                    )
                            )
            ) {

                output.writeInt(STORAGE_MAGIC);
                output.writeInt(STORAGE_VERSION);
                output.writeInt(avisos.size());

                for (
                        Aviso aviso :
                        avisos
                ) {

                    output.writeInt(
                            aviso.getId()
                    );

                    output.writeUTF(
                            aviso.getTitulo()
                    );

                    output.writeUTF(
                            aviso.getMensagem()
                    );

                    output.writeUTF(
                            aviso.getAutor()
                    );

                    output.writeLong(
                            aviso.getDataPublicacao()
                                    .toEpochDay()
                    );

                    output.writeUTF(aviso.getCriticidade());
                    output.writeUTF(aviso.getPrioridade());
                }
            }

            try {

                Files.move(
                        temporary,
                        storageFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (
                    AtomicMoveNotSupportedException error
            ) {

                Files.move(
                        temporary,
                        storageFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {

            /*
             * Caso alguma etapa falhe, tenta remover
             * o arquivo temporário.
             */
            try {

                Files.deleteIfExists(
                        temporary
                );

            } catch (IOException ignored) {
                // Não impede o erro original.
            }
        }
    }

    // ============================================================
    // VALIDAÇÃO
    // ============================================================

    private static void validarTexto(
            String valor,
            String mensagem
    ) {

        if (
                valor == null ||
                        valor.isBlank()
        ) {

            throw new IllegalArgumentException(
                    mensagem
            );
        }
    }

    // ============================================================
    // ARMAZENAMENTO PADRÃO
    // ============================================================

    private static Path defaultStorage() {

        String appData =
                System.getenv("APPDATA");

        Path baseDirectory;

        if (
                appData == null ||
                        appData.isBlank()
        ) {

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
                "historico-avisos.bin"
        );
    }
}
