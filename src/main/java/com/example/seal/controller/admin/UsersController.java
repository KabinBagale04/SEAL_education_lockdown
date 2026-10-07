package com.example.seal.controller.admin;
import com.example.seal.dto.UserResponse;
import com.example.seal.service.*;
import com.example.seal.session.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class UsersController {
    @FXML private VBox root;
    @FXML private TableView<UserResponse> usersTable;
    @FXML private TableColumn<UserResponse, String> nameColumn, usernameColumn, roleColumn, statusColumn;
    @FXML private Label messageLabel;
    @FXML private Button statusButton;
    private final AdminService service = new AdminService();
    @FXML private void initialize() {
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fullName()));
        usernameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().username()));
        roleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().role()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().active() ? "Active" : "Inactive"));
        usersTable.setPlaceholder(new Label("No teacher or student accounts."));
        usersTable.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> selection());
        selection();
        javafx.application.Platform.runLater(this::refresh);
    }
    private void selection() {
        UserResponse user = usersTable.getSelectionModel().getSelectedItem();
        statusButton.setDisable(user == null);
        statusButton.setText(user == null || user.active() ? "Deactivate" : "Activate");
    }
    @FXML private void refresh() {
        String token = UserSession.getToken();
        FxRequest.run(root, messageLabel, () -> service.getUsers(token), rows -> usersTable.getItems().setAll(rows));
    }
    @FXML private void changeStatus() {
        UserResponse user = usersTable.getSelectionModel().getSelectedItem();
        if (user == null) return;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, (user.active() ? "Deactivate " : "Activate ") + user.fullName() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirmation.initOwner(root.getScene().getWindow());
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        String token = UserSession.getToken();
        FxRequest.run(root, messageLabel, () -> service.setActive(user.id(), !user.active(), token), changed -> refresh());
    }
}
