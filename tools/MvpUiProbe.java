import com.example.seal.Navigator;
import com.example.seal.navigation.TeacherNavigator;
import com.example.seal.controller.teacher.TeacherWorkspace;
import com.example.seal.dto.*;
import com.example.seal.model.*;
import com.example.seal.service.*;
import com.example.seal.session.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Uses only MvpFixture on localhost:18081. Never run against production data. */
public class MvpUiProbe {
    private static Scene scene;
    private static Stage stage;
    private static <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(() -> {
            if (scene != null) { scene.getRoot().applyCss(); scene.getRoot().layout(); }
            return action.call();
        });
        Platform.runLater(task); return task.get(10, TimeUnit.SECONDS);
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void waitFor(BooleanSupplier test) throws Exception {
        for (int i=0; i<200; i++) { if (fx(test::getAsBoolean)) return; Thread.sleep(50); }
        throw new AssertionError("UI did not reach expected state: " + fx(() -> scene.getRoot().lookupAll(".label").stream().map(n -> ((Label)n).getText()).toList()));
    }
    private static void click(String label) {
        scene.getRoot().applyCss(); scene.getRoot().layout();
        Button button = scene.getRoot().lookupAll(".button").stream().filter(n -> n instanceof Button b && label.equals(b.getText()))
                .map(n -> (Button)n).findFirst().orElseThrow(() -> new AssertionError("Button not found: " + label));
        check(!button.isDisabled(), "Disabled button: " + label); button.fire();
    }
    private static void session(LoginResponse login) throws Exception {
        fx(() -> { UserSession.start(login.userId(),login.fullName(),login.role(),login.token()); return null; });
    }
    @SuppressWarnings("unchecked") private static <T> TableView<T> table(String id) { return (TableView<T>)scene.lookup("#" + id); }
    private static void shot(String name, int width, int height) throws Exception {
        fx(() -> { stage.setWidth(width); stage.setHeight(height); return null; });
        Thread.sleep(150);
        fx(() -> {
            WritableImage image = scene.getRoot().snapshot(null,null);
            BufferedImage png = new BufferedImage((int)image.getWidth(),(int)image.getHeight(),BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<png.getHeight();y++) for(int x=0;x<png.getWidth();x++) png.setRGB(x,y,image.getPixelReader().getArgb(x,y));
            ImageIO.write(png,"png",new File("/tmp/seal-mvp-" + name + ".png")); return null;
        });
    }
    public static void main(String[] args) throws Exception {
        System.setProperty("seal.api.baseUrl","http://localhost:18081");
        Platform.startup(() -> {});
        try {
            AuthService auth = new AuthService(); AdminService adminService = new AdminService();
            LoginResponse admin = auth.authenticate("mvp-test-admin","mvp-test-password");
            String suffix = UUID.randomUUID().toString().substring(0,8);
            String teacherName = "teacher-" + suffix, studentName = "student-" + suffix;
            adminService.createUser(new CreateUserRequest("Demo Teacher", teacherName,"test-password","TEACHER"), admin.token());
            UserResponse student = adminService.createUser(new CreateUserRequest("Demo Student",studentName,"test-password","STUDENT"),admin.token());
            session(admin);
            fx(() -> {
                scene = new Scene(new StackPane(),1100,700); stage = new Stage(); stage.setScene(scene); stage.show();
                Navigator.setScene(scene); Navigator.goTo("admin/admin-shell"); return null;
            });
            waitFor(() -> table("usersTable") != null && table("usersTable").getItems().size() >= 2);
            shot("admin",1100,700);
            LoginResponse teacher = auth.authenticate(teacherName,"test-password");
            session(teacher);
            fx(() -> { Navigator.goTo("teacher/teacher-shell"); return null; });
            waitFor(() -> table("table") != null && !table("table").isDisabled());
            Exam form = new Exam(); form.setTitle("MVP live exam"); form.setSubject("Computing"); form.setSubjectCode("CS101");
            form.setDurationMinutes(15); form.setInstructions("Answer all questions."); form.setStatus(ExamStatus.DRAFT);
            Question mcq = new Question(); mcq.setType(QuestionType.MCQ); mcq.setQuestionText("Which format is used by REST?");
            mcq.setMarks(2); mcq.setOptions(List.of("JSON","FXML","CSS","JDBC")); mcq.setCorrectOption(0);
            Question text = new Question(); text.setType(QuestionType.TEXT); text.setQuestionText("Why use an API?");
            text.setMarks(5); text.setReferenceAnswer("Central authorization and data access.");
            form.setQuestions(List.of(mcq,text));
            fx(() -> { TeacherWorkspace.editing = form; TeacherNavigator.goTo("create-exam"); return null; });
            shot("teacher-editor",1100,700);
            fx(() -> { click("Save Draft"); return null; });
            waitFor(() -> table("table") != null && table("table").getItems().size() == 1);
            fx(() -> { table("table").getSelectionModel().selectFirst(); click("Edit draft"); return null; });
            waitFor(() -> scene.lookup("#titleField") != null);
            fx(() -> { check(((TextField)scene.lookup("#titleField")).getText().equals("MVP live exam"),"Draft reload");
                click("Publish Exam"); return null; });
            waitFor(() -> table("table") != null && table("table").getItems().size() == 1
                    && ((Exam)table("table").getItems().get(0)).getStatus() == ExamStatus.PUBLISHED);
            Exam published = fx(() -> (Exam)table("table").getItems().get(0));
            check(published.getAccessCode() != null,"Live access code");
            shot("teacher-exams",900,600);
            fx(() -> {
                check(table("table").getFixedCellSize() == 46, "Teacher shared table CSS loaded");
                return null;
            });
            LoginResponse studentLogin = auth.authenticate(studentName,"test-password");
            session(studentLogin);
            fx(() -> {
                Navigator.goTo("student-home"); scene.getRoot().applyCss(); scene.getRoot().layout();
                check(scene.lookup("#registration") == null, "Student starts on dashboard");
                ((ToggleButton)scene.lookup("#examsButton")).fire();
                scene.getRoot().applyCss(); scene.getRoot().layout();
                ((TextField)scene.lookup("#registration")).setText("REG-TEST");
                ((TextField)scene.lookup("#symbol")).setText("SYM-TEST");
                ((TextField)scene.lookup("#code")).setText(published.getAccessCode());
                click("Continue"); return null;
            });
            waitFor(() -> scene.lookup("#accepted") != null);
            fx(() -> { ((CheckBox)scene.lookup("#accepted")).setSelected(true); click("Start exam"); return null; });
            waitFor(() -> scene.lookup("#answerArea") != null);
            fx(() -> { ((RadioButton)scene.lookup(".radio-button")).fire(); click("Next");
                ((TextArea)scene.lookup(".text-area")).setText("The server controls authorization and database access.");
                return null;
            });
            shot("student-exam",1100,700);
            fx(() -> { click("Submit exam"); click("Confirm submission"); return null; });
            waitFor(() -> scene.lookup("#reference") != null);
            fx(() -> { check(!((Label)scene.lookup("#reference")).getText().startsWith("DEMO"),"Real API receipt"); return null; });
            shot("student-receipt",900,600);
            session(teacher);
            fx(() -> { Navigator.goTo("teacher/teacher-shell"); TeacherNavigator.goTo("results"); return null; });
            waitFor(() -> table("table") != null && table("table").getItems().size() == 1);
            fx(() -> { table("table").getSelectionModel().selectFirst(); return null; });
            waitFor(() -> scene.lookup(".spinner") != null);
            fx(() -> { ((Spinner<Integer>)scene.lookup(".spinner")).getValueFactory().setValue(4); return null; });
            shot("teacher-grading",1100,700);
            fx(() -> { click("Save text grades"); return null; });
            waitFor(() -> table("table").getItems().size() == 1 && "GRADED".equals(((JsonNode)table("table").getItems().get(0)).get("status").asText()));
            session(studentLogin);
            fx(() -> {
                Navigator.goTo("student-home");
                ((ToggleButton)scene.lookup("#resultsButton")).fire();
                return null;
            });
            waitFor(() -> table("table").getItems().size() == 1);
            fx(() -> { check(((JsonNode)table("table").getItems().get(0)).get("totalScore").asInt() == 6,"Final grade 6/7"); return null; });
            shot("student-results",900,600);
            fx(() -> {
                check(table("table").getFixedCellSize() == 46, "Student shared table CSS loaded");
                check(((ToggleButton)scene.lookup("#resultsButton")).isSelected(), "Results selection");
                ((ToggleButton)scene.lookup("#dashboardButton")).fire();
                scene.getRoot().applyCss(); scene.getRoot().layout();
                check(scene.lookup("#pastPapers") != null, "Results returns to dashboard");
                check(scene.lookup("#registration") == null, "No access fields on dashboard");
                return null;
            });
            fx(() -> { SessionActions.logout(); return null; });
            waitFor(() -> scene.lookup("#usernameField") != null);
            try { new ApiClient().request("GET","/api/student/exams/results",null,studentLogin.token()); throw new AssertionError("Logout token remains valid"); }
            catch (IllegalStateException expected) {}
            adminService.setActive(student.id(),false,admin.token());
            check(adminService.getUsers(admin.token()).stream().filter(u->u.id().equals(student.id())).noneMatch(UserResponse::active),"Deactivation persisted");
            System.out.println("PASS: real admin/teacher/student REST adapters, FXML draft/save/edit/publish, exam submission, manual grading, results, logout and deactivation");
        } finally {
            fx(() -> { if(stage!=null)stage.close(); return null; }); Platform.exit();
        }
    }
}
