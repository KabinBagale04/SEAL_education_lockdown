package com.example.seal.controller.admin;

import com.example.seal.dto.UserResponse;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class UsersController {
    @FXML
    private TableView<UserResponse> usersTable;

    //rows are userresponse object and string is the datatype of the row
    @FXML
    private TableColumn<UserResponse, String> nameColumn;

    @FXML
    private TableColumn<UserResponse, String> usernameColumn;

    @FXML
    private TableColumn<UserResponse, String> roleColumn;

    @FXML
    private TableColumn<UserResponse, String> statusColumn;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize(){
        nameColumn.setCellValueFactory(cellData->
            new SimpleStringProperty(
                    cellData.getValue().fullName()
            )
        );
        usernameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().username()
                )
        );

        roleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().role()
                )
        );

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().active()
                                ? "Active"
                                : "Inactive"
                )
        );
    }
}
