module client {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.rmi;
    requires rmigameserver;
    requires rmigameclient;
    requires rmimainserver;
    requires rmigamesession;
    requires java.persistence;
    exports client.mainui.login;
    exports client.mainui.highscoretableview;
    exports client.mainui.createuser;

    opens UI;
    opens client.game.tictactoe;
    opens client.mainui;
    opens client.mainui.login to javafx.fxml, javafx.graphics;
    opens client.mainui.createuser to javafx.fxml, javafx.graphics;
    opens client.mainui.highscoretableview;
}