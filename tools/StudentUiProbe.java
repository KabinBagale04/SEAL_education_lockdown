import com.example.seal.Navigator;
import com.example.seal.session.UserSession;
import com.example.seal.student.StudentFlow;
import com.example.seal.student.model.ExamDtos.*;
import com.example.seal.student.service.PendingExamService;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Local JavaFX smoke probe, never packaged as production authentication or a demo entry point. */
public final class StudentUiProbe {
    private static Scene scene;
    private static Stage stage;
    private static <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void waitFor(BooleanSupplier test) throws Exception {
        for (int i = 0; i < 100; i++) {
            if (fx(test::getAsBoolean)) return;
            Thread.sleep(50);
        }
        throw new AssertionError("Timed out waiting for page transition");
    }
    private static Button button(String id) {
        scene.getRoot().applyCss(); scene.getRoot().layout();
        return (Button) scene.lookup("#" + id);
    }
    private static void capture(String name, int width, int height) throws Exception {
        fx(() -> {
            stage.setWidth(width); stage.setHeight(height);
            scene.getRoot().applyCss(); scene.getRoot().layout();
            return null;
        });
        Thread.sleep(100);
        fx(() -> {
            WritableImage image = scene.getRoot().snapshot(null, null);
            BufferedImage output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < output.getHeight(); y++) for (int x = 0; x < output.getWidth(); x++)
                output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            ImageIO.write(output, "png", new File("/tmp/seal-student-" + name + ".png"));
            return null;
        });
    }
    public static void main(String[] args) throws Exception {
        System.setProperty("seal.student.demo", "true");
        Platform.startup(() -> { });
        try {
            fx(() -> {
                UserSession.start(100L, "UI Test Student", "STUDENT", "local-test-token");
                scene = new Scene(new StackPane(), 1100, 700);
                stage = new Stage(); stage.setScene(scene); stage.show();
                Navigator.setScene(scene); Navigator.goTo("student-home");
                button("continueButton").fire();
                check(((Label) scene.lookup("#message")).getText().contains("Enter your"), "Required fields");
                ((TextField) scene.lookup("#registration")).setText("REG-TEST");
                ((TextField) scene.lookup("#symbol")).setText("SYM-TEST");
                ((TextField) scene.lookup("#code")).setText("DEMO");
                return null;
            });
            capture("access", 1100, 700);
            fx(() -> { button("continueButton").fire(); return null; });
            waitFor(() -> scene.lookup("#accepted") != null);
            capture("instructions", 900, 600);
            fx(() -> {
                check(button("start").isDisabled(), "Instruction acknowledgement required");
                ((CheckBox) scene.lookup("#accepted")).setSelected(true);
                button("start").fire(); return null;
            });
            waitFor(() -> scene.lookup("#answerArea") != null);
            fx(() -> {
                ((RadioButton) scene.lookup(".radio-button")).fire();
                button("next").fire();
                ((TextArea) scene.lookup(".text-area")).setText("A service boundary keeps network details outside the UI.");
                button("previous").fire();
                check(((RadioButton) scene.lookup(".radio-button")).isSelected(), "MCQ answer retained");
                button("next").fire();
                check(((TextArea) scene.lookup(".text-area")).getText().startsWith("A service"), "Text answer retained");
                button("submit").fire();
                check(((Label) scene.lookup("#confirmationText")).getText().contains("1 question"), "Unanswered count");
                return null;
            });
            capture("confirmation", 900, 600);
            fx(() -> { button("cancel").fire(); return null; });
            capture("exam", 1100, 700);
            fx(() -> { button("submit").fire(); button("confirm").fire(); return null; });
            waitFor(() -> scene.lookup("#reference") != null);
            capture("acknowledgement", 900, 600);
            fx(() -> {
                StudentFlow flow = StudentFlow.current();
                check(flow.receipt.reference().startsWith("DEMO-"), "Clearly labelled demo receipt");
                check(flow.service.submit(flow.submission(), "test").equals(flow.receipt), "Idempotent demo submission");
                flow.returnToAccess();
                check(flow.answers.isEmpty(), "Attempt reset");
                flow.access = flow.service.validateAccess(new AccessRequest("r", "s", "DEMO"), "test");
                Attempt attempt = flow.service.startExam(flow.access.accessId(), flow.startRequestId, "test");
                check(flow.service.startExam(flow.access.accessId(), flow.startRequestId, "test").equals(attempt), "Idempotent demo start");
                flow.attempt = new Attempt(attempt.id(), attempt.title(), Instant.now().minusSeconds(1), attempt.questions());
                flow.go("examination"); return null;
            });
            waitFor(() -> scene.lookup("#reference") != null);
            fx(() -> {
                UserSession.start(101L, "Next student", "STUDENT", "different-token");
                check(StudentFlow.current().attempt == null, "Session change clears attempt");
                try {
                    new PendingExamService().validateAccess(new AccessRequest("r", "s", "c"), "test");
                    throw new AssertionError("Pending service must not fake success");
                } catch (IllegalStateException expected) { }
                return null;
            });
            System.out.println("PASS: FXML, required fields, instructions, navigation, answer retention, confirmation, submission, expiry, session isolation, pending boundary");
        } finally {
            fx(() -> { if (stage != null) stage.close(); return null; });
            Platform.exit();
        }
    }
}
