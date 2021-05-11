package client.mainui.createuser;

import UI.AlertBox;
import client.mainui.MainController;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import rmimainserver.RMIMainServer;

import java.rmi.RemoteException;

public class CreateUserController {
    @FXML
    TextField username;
    @FXML
    PasswordField password;
    @FXML
    PasswordField confirm;
    MainController main;
    RMIMainServer srv;

    public void setSrv(RMIMainServer srv) {
        this.srv = srv;
    }

    public void setMain(MainController main) {
        this.main = main;
    }


    @FXML
    public String createButtonPressed(Event e) {
        // retrieve user+pass
        String user = username.getText();
        String pass = password.getText();
        String confirmPass = confirm.getText();
        try {
            // check if username and password are valid
            if (pass.equals("") || user.equals("")) {
                confirm.setText("");
                new AlertBox(Alert.AlertType.INFORMATION,
                        "Invalid username or password.\nPlease try again",
                        true).show();

            }
            //check password confirmation
            else if (!pass.equals(confirmPass)) {
                confirm.setText("");
                new AlertBox(Alert.AlertType.INFORMATION,
                        "password confirmation failed.\nPlease retype password.",
                        true).show();
            }
            //try to create user
            else if (srv.createUser(user, pass)) {
                Node source = (Node) e.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
                stage.close();
                new AlertBox(Alert.AlertType.INFORMATION,
                        "User created successfully!\nPlease sign in.",
                        true).show();
            } else {
                username.setText("");
                password.setText("");
                confirm.setText("");
                new AlertBox(Alert.AlertType.INFORMATION,
                        "Username taken, please choose another one.",
                        true).show();
            }
        } catch (RemoteException remoteException) {
            System.err.println("main server sql error");
            remoteException.printStackTrace();
        }
        return username.getText();
    }
}
