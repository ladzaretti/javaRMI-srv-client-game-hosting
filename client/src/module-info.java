module client {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.rmi;
    requires rmigameserver;
    requires rmigameclient;
    opens GameClient;
}