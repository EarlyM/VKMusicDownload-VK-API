package ua.vk.music.download.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ua.vk.music.download.model.HttpSender;
import ua.vk.music.download.object.User;

public class LoginController {

    @FXML
    private TextField name;

    @FXML
    private PasswordField password;

    public void loginButton(ActionEvent actionEvent) {
        User.createUser(name.getText(), password.getText());
        try {
            HttpSender.getInstance().authorize();
            ((Node)actionEvent.getSource()).getScene().getWindow().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
