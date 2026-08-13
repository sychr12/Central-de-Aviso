package com.example.intranet_adm.service;

import com.example.intranet_adm.model.Aviso;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Histórico local dos avisos efetivamente enviados pela Central. */
public class AvisoService {
    private static final Path DEFAULT_STORAGE = defaultStorage();

    private final List<Aviso> avisos = new ArrayList<>();
    private final Path storageFile;
    private int proximoId = 1;

    public AvisoService() {
        this(DEFAULT_STORAGE);
    }

    AvisoService(Path storageFile) {
        this.storageFile = storageFile;
        carregar();
    }

    public synchronized Aviso adicionar(String titulo, String mensagem, String autor) {
        Aviso aviso = new Aviso(proximoId++, titulo, mensagem, autor, LocalDate.now());
        avisos.add(0, aviso);
        try {
            salvar();
        } catch (IOException error) {
            avisos.remove(aviso);
            proximoId--;
            throw new IllegalStateException("Não foi possível salvar o histórico local.", error);
        }
        return aviso;
    }

    public synchronized boolean remover(int id) {
        boolean removed = avisos.removeIf(aviso -> aviso.getId() == id);
        if (!removed) return false;
        try {
            salvar();
        } catch (IOException error) {
            carregar();
            throw new IllegalStateException("Não foi possível atualizar o histórico local.", error);
        }
        return true;
    }

    public synchronized List<Aviso> listarTodos() {
        return Collections.unmodifiableList(new ArrayList<>(avisos));
    }

    private synchronized void carregar() {
        avisos.clear();
        proximoId = 1;
        if (!Files.exists(storageFile)) return;

        try (DataInputStream input = new DataInputStream(Files.newInputStream(storageFile))) {
            int total = input.readInt();
            if (total < 0 || total > 10_000) throw new IOException("Quantidade de avisos inválida.");
            for (int index = 0; index < total; index++) {
                Aviso aviso = new Aviso(
                        input.readInt(), input.readUTF(), input.readUTF(), input.readUTF(),
                        LocalDate.ofEpochDay(input.readLong()));
                avisos.add(aviso);
                proximoId = Math.max(proximoId, aviso.getId() + 1);
            }
        } catch (IOException error) {
            System.err.println("Não foi possível carregar o histórico de avisos: " + error.getMessage());
            avisos.clear();
            proximoId = 1;
        }
    }

    private void salvar() throws IOException {
        Files.createDirectories(storageFile.getParent());
        Path temporary = storageFile.resolveSibling(storageFile.getFileName() + ".tmp");
        try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(temporary))) {
            output.writeInt(avisos.size());
            for (Aviso aviso : avisos) {
                output.writeInt(aviso.getId());
                output.writeUTF(aviso.getTitulo());
                output.writeUTF(aviso.getMensagem());
                output.writeUTF(aviso.getAutor());
                output.writeLong(aviso.getDataPublicacao().toEpochDay());
            }
        }
        try {
            Files.move(temporary, storageFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException error) {
            Files.move(temporary, storageFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path defaultStorage() {
        String appData = System.getenv("APPDATA");
        Path baseDirectory = appData == null || appData.isBlank()
                ? Path.of(System.getProperty("user.home"), ".intranet-adm")
                : Path.of(appData, "Intranet-IDAM");
        return baseDirectory.resolve("historico-avisos.bin");
    }
}
