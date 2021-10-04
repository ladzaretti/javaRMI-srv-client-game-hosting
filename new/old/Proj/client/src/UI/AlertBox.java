package UI;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AlertBox extends Alert {
    public AlertBox(Alert.AlertType type, String msg, boolean showButton) {
        super(type);
        setHeaderText(null);
        setTitle("Information Dialog");
        setContentText(msg);
        ButtonType buttonTypeOne = new ButtonType("OK");
        getButtonTypes().setAll(buttonTypeOne);
        getDialogPane().lookupButton(buttonTypeOne).setVisible(showButton);
    }
}
