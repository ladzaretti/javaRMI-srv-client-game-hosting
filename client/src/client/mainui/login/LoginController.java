package client.mainui.login;

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

// login screen controller
public class LoginController {
    @FXML
    TextField username;
    @FXML
    PasswordField password;
    MainController main;
    RMIMainServer srv;

    public void setSrv(RMIMainServer srv) {
        this.srv = srv;
    }

    public void setMain(MainController main) {
        this.main = main;
    }


    @FXML
    // handle button press
    public void loginButtonPressed(Event e) {
        // retrieve user+pass
        String user = username.getText();
        String pass = password.getText();


        try {
            if (srv.signIn(user, pass)) {
                // if successful close window and update main controller
                // regarding username.
                Node source = (Node) e.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
                stage.close();
                main.setUserLogged(true);
                main.setUsername(user);

                // show appropriate message dialog
                new AlertBox(Alert.AlertType.INFORMATION,
                        "Login successful\nSelect game type and then start the game.",
                        true).show();
            } else {
                // sign in failed
                username.setText("");
                password.setText("");
                new AlertBox(Alert.AlertType.INFORMATION,
                        "Username or password incorrect.\nPlease try again.",
                        true).show();
            }
        } catch (RemoteException remoteException) {
            System.err.println("main server sql error");
            remoteException.printStackTrace();
        }
    }
}
