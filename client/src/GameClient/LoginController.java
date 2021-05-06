package GameClient;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    TextField username;
    @FXML
    PasswordField password;
    MainController main;

    public void setMain(MainController main) {
        this.main = main;
    }


    @FXML
    public String loginButtonPressed(Event e) {
        // retrieve user+pass
        // todo verify if password is legal
        // todo connect to sql
        String userpass = username.getText() + "\n" + password.getText();

        //close the login screen if successful
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
        main.setUserLogged(true);
        new AlertBox(Alert.AlertType.INFORMATION,
                "Login successful\nSelect game type and then start the game.",
                true).show();
        return userpass;
    }
}
