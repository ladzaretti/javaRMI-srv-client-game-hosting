package GameClient;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import rmigameclient.RMIGameClient;
import rmigameserver.RMIGameServer;

public class MainController {
    @FXML
    private MenuItem connectMenuItem;
    @FXML
    private String connectionInfo;
    @FXML
    private VBox vbox;

    rmigameserver.RMIGameServer srvStub = null;
    rmigameclient.RMIGameClient gameStub = null;
    Boolean connected = false;
    AlertBox alert;

    public void initialize() {
        vbox.sceneProperty().addListener((observableScene,
                                          oldScene,
                                          newScene) -> {
            if (oldScene == null && newScene != null) {
                // scene is set for the first time.
                // listen to stage changes.
                newScene.windowProperty().addListener((observableWindow,
                                                       oldWindow,
                                                       newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        // stage is set. now is the right time to do
                        // whatever we need to the stage in the controller.
                        // set on close request for the main window
                        newWindow.setOnCloseRequest(windowEvent -> {
                            System.out.println("main window closed");
                            try {
                                if (connected)
                                    srvStub.disconnect(gameStub);
                                System.exit(0);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                });
            }
        });
    }

    GameClient game = null;

    @FXML
    public void connectButtonClicked() {
        connectMenuItem.setDisable(true);

        //set connection to the remote server
        Registry reg;
        try {
            reg = LocateRegistry.getRegistry(null, 1777);
            srvStub = (RMIGameServer) reg.lookup("GameServer");
            gameStub = (RMIGameClient) UnicastRemoteObject.exportObject(
                    game = new GameClient(this), 0);
            connected = true;
            Thread t = new Thread(game);
            t.start();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        //create alert box
        alert = new AlertBox(Alert.AlertType.INFORMATION,
                connected ? "please wait" : "connection error",
                connected);


        //set alertBox on close action -> disconnect
        Window alertWin = alert.getDialogPane().getScene().getWindow();
        alertWin.setOnCloseRequest(windowEvent -> {
            try {
                System.out.println("closed");
                if (connected)
                    srvStub.disconnect(gameStub);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        alert.show();

        //create background thread to handle matchmaking
        //so the UI stays responsive
        Task<String> connect = new Task<>() {
            @Override
            protected String call() {
                String srvAns = null;
                try {
                    srvAns = srvStub.connect(gameStub);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return srvAns;
            }
        };
        connect.setOnSucceeded(e -> {
            connectionInfo = connect.getValue();
            System.out.println(connectionInfo);

        });
        new Thread(connect).start();
    }

    public void closeWaitingBox() {
        Platform.runLater(() ->
                alert.close()
        );
    }
}