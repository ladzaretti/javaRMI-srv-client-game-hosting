package GameClient;

import javafx.fxml.FXML;
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
    public String loginButtonPressed() {
        String userpass = username.getText() + "\n" + password.getText();
        main.print(userpass);
        return userpass;
    }
}
