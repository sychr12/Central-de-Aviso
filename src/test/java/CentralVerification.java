import com.example.intranet_adm.service.*;
import com.example.intranet_adm.view.CentralAvisosView;
import com.example.intranet_adm.view.aviso.AvisoFormDates;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.ButtonBase;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.concurrent.*;

/** Standalone regression and scene-rendering checks; never publishes to the intranet. */
public class CentralVerification {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        System.setProperty("intranet.base.url", "http://127.0.0.1:9");
        System.setProperty("intranet.avisos.url", "http://127.0.0.1:9/custom");
        check(IntranetAvisosClient.endpoint().equals("http://127.0.0.1:9/custom"), "Explicit endpoint ignored");
        System.clearProperty("intranet.avisos.url");
        check(IntranetAvisosClient.endpoint().equals("http://127.0.0.1:9/api/avisos"), "Base URL ignored");
        Path storage = Files.createTempDirectory("central-verification").resolve("history.dat");
        AvisoService service = new AvisoService(storage);
        var notice = service.adicionar("Teste", "Mensagem com acentuação", "QA");
        check(new AvisoService(storage).quantidade() == 1, "History not persisted");
        service.remover(notice.getId());
        check(new AvisoService(storage).quantidade() == 0, "Deletion not persisted");
        Path corrupt = storage.resolveSibling("corrupt.dat");
        byte[] original = new byte[] {1, 2, 3};
        Files.write(corrupt, original);
        try {
            new AvisoService(corrupt).adicionar("Teste", "Mensagem", "QA");
            throw new AssertionError("Corrupt history was overwritten");
        } catch (IllegalStateException expected) {
            check(java.util.Arrays.equals(original, Files.readAllBytes(corrupt)), "Original history changed");
        }
        CountDownLatch done = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> failure = new java.util.concurrent.atomic.AtomicReference<>();
        Platform.startup(() -> {
            try {
                io.github.palexdev.materialfx.theming.UserAgentBuilder.builder()
                    .themes(io.github.palexdev.materialfx.theming.JavaFXThemes.MODENA)
                    .themes(io.github.palexdev.materialfx.theming.MaterialFXStylesheets.forAssemble(true))
                    .setDeploy(true).setResolveAssets(true).build().setGlobal();
                Stage stage = new Stage();
                Parent root = CentralAvisosView.criar(stage);
                Scene scene = new Scene(root, 1280, 820);
                scene.getStylesheets().add(CentralVerification.class.getResource("/com/example/intranet_adm/style.css").toExternalForm());
                stage.setScene(scene);
                render(root, "target/central-redesign.png", 1280, 820);
                render(root, "target/central-compact.png", 1000, 680);
                var nav = new java.util.ArrayList<ButtonBase>();
                for (Node node : root.lookupAll(".central-nav")) if (node instanceof ButtonBase button) nav.add(button);
                for (ButtonBase button : nav) {
                    if (button.getText().contains("Sair")) continue;
                    button.fire();
                    render(root, "target/screen-" + nav.indexOf(button) + ".png", 1280, 820);
                }
                if (stage.getOnHidden() != null) stage.getOnHidden().handle(null);
                check(new AvisoFormDates().getPublicarEm() == null, "Immediate publication should not schedule");
            } catch (Throwable error) { failure.set(error); }
            finally { done.countDown(); }
        });
        if (!done.await(60, TimeUnit.SECONDS)) throw new AssertionError("UI verification timed out");
        Platform.exit();
        if (failure.get() != null) throw new AssertionError("UI verification failed", failure.get());
        System.out.println("PASS: endpoint precedence, history persistence, navigation and scene rendering");
    }
    private static void render(Parent root, String file, int width, int height) throws Exception {
        root.resize(width, height); root.applyCss(); root.layout();
        WritableImage image = root.snapshot(null, new WritableImage(width, height));
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
        ImageIO.write(output, "png", Path.of(file).toFile());
    }
}
