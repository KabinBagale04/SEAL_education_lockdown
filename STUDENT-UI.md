# SEAL Student Client

This implementation adds only the JavaFX student flow. Existing login, role routing,
Navigator, teacher/admin pages, and backend authentication remain unchanged.

## Complete Files

```text
src/main/java/com/example/seal/
  controller/student/
    StudentPage.java
    AccessController.java
    InstructionsController.java
    ExaminationController.java
    AcknowledgementController.java
  student/
    StudentFlow.java
    model/ExamDtos.java
    service/ExamService.java
    service/PendingExamService.java
    service/DemoExamService.java
src/main/resources/com/example/seal/
  student-home.fxml
  student/instructions.fxml
  student/examination.fxml
  student/acknowledgement.fxml
  css/student.css
src/main/java/module-info.java
tools/StudentUiProbe.java
tools/check-student-ui.sh
```

The existing module descriptor gains only:

```java
opens com.example.seal.controller.student to javafx.fxml;
```

No Maven dependencies are added. Java source targets release 21 and uses JavaFX 21.

## Navigation

Existing STUDENT login already calls `Navigator.goTo("student-home")`.
That resource is now Exam Access. Successful validation leads to
`student/instructions`, Start leads to `student/examination`, and an acknowledged
submission leads to `student/acknowledgement`. Return resets attempt data and opens
`student-home`. All transitions use the existing Navigator and Scene; the production
student code creates no Stage. Submit confirmation is inline on the exam page.

StudentFlow holds one attempt and its answers across pages, separated from UserSession.
The access code and registration fields are passed to the service; controllers contain
no exam fixtures. Question DTOs deliberately contain no correct answer or reference answer.
The timer uses the service-provided expiry instant; Timeline only schedules clock updates,
with no visual animations. Question changes preserve responses in memory. Expiry locks
editing and attempts submission. A failed submission retains a frozen payload and request
ID for retry, since a lost response does not establish whether the server accepted it.

## Backend Integration Pending

The inspected server currently exposes auth/admin-user functionality, not student exam
endpoints. Normal operation therefore uses PendingExamService, which reports unavailable
access instead of returning fictional success. The following are client contracts, not
claims about existing REST endpoints:

| Method | Input | Expected response |
| --- | --- | --- |
| validateAccess | Registration number, symbol number, access code, existing bearer token | Exam access ID, title, instructions, duration, question count |
| startExam | Access ID, stable request ID, existing bearer token | Attempt ID, expiry instant, student-safe questions and options |
| submit | Attempt ID, stable request ID, answers, existing bearer token | Submission reference and acknowledgement message |

Implement ExamService using the project's HTTP/JSON conventions when actual routes and
wire DTOs are available, then replace the PendingExamService selection in StudentFlow.
Use the existing token in Authorization: Bearer; HTTPS for remote traffic. Calls already
run in JavaFX Tasks. Map service failures to safe user-facing messages; do not expose raw
server response bodies. Configure finite connection/request timeouts. Agree retry
idempotency with the server for start and submit. The server must enforce exam access,
ownership and time limits; the display timer is not a security boundary. Confirm server
time/clock offset handling before production timed exams. No database access is added.

An acknowledgement does not imply a grade. Results/grades need an agreed backend contract.
There is no persistence or resume after application exit yet; answers are in memory only.
The UI adds no OS lockdown and does not prevent application closure.

## Explicit Demo Mode

Set `-Dseal.student.demo=true` in the application JVM options to select DemoExamService.
For a Maven launch from the client root:

```sh
JAVA_TOOL_OPTIONS='-Dseal.student.demo=true' ./mvnw javafx:run
```

Use the existing authentication and a student account. Enter any nonblank registration
and symbol numbers, and the exact access code `DEMO`. Every page displays a demo label.
Demo answers stay local and receipts explicitly say nothing was saved to the server.
All fixture questions live in DemoExamService. Omit the flag to return to the pending
integration boundary. The UI probe's simulated session is test-only, not an app login route.

## Future Updates

Student dashboard and access to older question papers are intentionally deferred.
They need a teacher-authorized published-paper/history contract and must not reveal active
exam questions or answer keys. No dashboard page, productivity sidebar, or archive endpoint
is implemented in this increment.

## Verification

The local smoke probe loads actual FXML, uses the existing Navigator, exercises required
fields, instruction acceptance, MCQ/text retention, submit/cancel confirmation,
acknowledgement, automatic expiry submission, repeat request IDs, and session isolation.
It also captures page snapshots at 1100x700 and 900x600. It requires a graphical JavaFX
runtime. Runtime verification on this machine uses installed JDK 26 with release-21
compilation; execution on JDK 21 still needs confirmation.
No backend or live database integration is tested because student endpoints are absent.

Run `sh tools/check-student-ui.sh` from the client root with desktop access. It compiles
against release 21 and runs the FXML probe on the module path, checking the actual module
descriptor. The first Maven dependency download requires network access; the script
assumes dependencies are already cached and runs Maven offline.
