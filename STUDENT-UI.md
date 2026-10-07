# SEAL Client MVP

## Working Flow

Existing Navigator and role routing are retained; pages reuse one Scene, not new Stages.

- Admin: add teacher/student, load users, activate/deactivate selected accounts.
- Teacher: Dashboard/Exams lists actual exams; create/save draft, edit draft, publish,
  read access code, close; Results lists attempts and allows manual text grading.
- Student: login -> Dashboard, with Dashboard / Exams / Results sidebar navigation.
  Past papers is a clearly labelled Coming soon tile, not a working paper archive.
  Exams opens the registration number, symbol number and access-code form.
  Valid access -> Instructions -> Examination -> confirmation -> acknowledgement.
  The timed exam remains full-screen. Return to dashboard or My results after submission.
  Results displays only the student's submitted/graded summaries from the backend.

The question editor supports MCQs and text questions. Answers survive question navigation
in memory. The exam timer uses server-provided expiry; timeout freezes and submits answers.
Failed submission retains the payload for an identical retry.

## Client Boundaries

ApiClient is shared HTTPS/JSON transport using the existing UserSession bearer token.
AuthService preserves the existing login contract; logout invalidates the server token.
FxRequest runs admin/teacher/result calls outside the JavaFX thread.
TeacherExamService handles exam authoring and grading API calls.
student.service.ExamService remains the student boundary.
RestExamService is now the default implementation; it maps actual server responses into
the student DTOs. DemoExamService is isolated and enabled only by an explicit flag.

FXML/controller/CSS separation is retained. Student controllers have no hardcoded exam
fixtures. Student DTOs contain no answer keys. No direct PostgreSQL connection exists.

StudentShellController and StudentNavigator manage pages inside the student workspace.
Navigator.goTo still owns application-level transitions and reuses the same Scene.
teacher.css and student.css import controls.css for consistent tables, dropdowns,
scrollbars and buttons. The student sidebar shows the active page.

## Run

Start the backend first, then from this client repository:

    ./mvnw javafx:run

Default API URL: http://localhost:8080 (local development only).
For a deployed backend, use HTTPS:

    JAVA_TOOL_OPTIONS='-Dseal.api.baseUrl=https://your-server.example' ./mvnw javafx:run

Demo mode is optional, not the default:

    JAVA_TOOL_OPTIONS='-Dseal.student.demo=true' ./mvnw javafx:run

Demo access code is DEMO with nonblank registration/symbol fields.
Demo receipts explicitly identify local data. My results still uses the real backend.

## Presentation Demonstration

1. Admin signs in, creates a teacher/student and checks the Users list.
2. Teacher creates an MCQ + text exam, saves it, edits the draft and publishes.
3. Student logs in, enters registration/symbol/access code, accepts instructions,
   answers both questions and submits. Acknowledgement shows pending text grading.
4. Teacher opens Results, selects the attempt and awards text marks.
5. Student opens My results to see the final score.
6. Admin deactivates the student; the old token no longer authorizes requests.

Study the path: FXML event -> controller -> client service -> REST controller ->
backend service -> repository -> database -> response DTO -> JavaFX update.

## Verification and Limits

sh tools/check-mvp-ui.sh runs the real FXML + REST test against the server's isolated
MvpFixture on port 18081. See EXAM-BACKEND.md in the server repository for fixture startup.
It covers admin users, teacher draft/edit/publish, student access/submit, manual grading,
student results, logout and deactivation. Screenshots are written under /tmp.
sh tools/check-student-ui.sh separately covers the isolated student demo/timeout flow.

The student dashboard is implemented; older question papers remain a future update.
Registration/symbol information is collected, not institutionally verified.
Answers have no durable local recovery after app exit. Closing an exam blocks new
submissions. No OS-level lockdown or monitoring is implemented.
Tests use H2 and JDK 26 with release-21 compilation; real PostgreSQL deployment and
runtime execution on JDK 21 still need verification.
