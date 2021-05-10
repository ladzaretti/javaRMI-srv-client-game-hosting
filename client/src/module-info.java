module client {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.rmi;
    requires rmigameserver;
    requires rmigameclient;
    requires rmimainserver;
    requires rmigamesession;
    exports client.mainui.login;

    opens UI;
    opens client.game.tictactoe;
    opens client.mainui;
    opens client.mainui.login to javafx.fxml, javafx.graphics;
}