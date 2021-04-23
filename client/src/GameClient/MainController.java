package GameClient;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import rmigameserver.RMIGameServer;

public class MainController {
    @FXML
    private MenuItem connectMenuItem;
    @FXML
    private MenuBar menuBar;
    @FXML
    private String connectionInfo;
    rmigameserver.RMIGameServer stub = null;
    Boolean connected = false;

    @FXML
    public void ConnectButtonClicked() {
        System.out.println("connect");
        connectMenuItem.setDisable(true);
        AlertBox alert;

        //set connection to the remote server
        Registry reg;
        try {
            reg = LocateRegistry.getRegistry(null, 1777);
            stub = (RMIGameServer) reg.lookup("GameServer");
            connected = true;
        } catch (ConnectException e) {
            System.out.println("connection error");
            e.printStackTrace();
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
                    stub.disconnect();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        alert.show();

        //create background thread to handle matchmaking
        //so the UI stays responsive
        Task<String> connect = new Task<>() {
            @Override
            protected String call() throws Exception {
                return stub.connect();
            }
        };
        connect.setOnSucceeded(e -> {
            alert.close();
            connectionInfo = connect.getValue();
            System.out.println(connectionInfo);

        });
        new Thread(connect).start();
    }
}
