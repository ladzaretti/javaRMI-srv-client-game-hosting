package GameClient;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

import rmigameclient.RMIGameClient;
import rmigameserver.RMIGameServer;
import rmimainserver.RMIMainServer;

public class MainController {
    @FXML
    private MenuItem connectMenuItem;
    @FXML
    private MenuItem startGameMenu;
    @FXML
    private String connectionInfo;
    @FXML
    private VBox vbox;
    @FXML
    private MenuItem gameTypeMenu;
    @FXML
    private MenuItem createUserMenuItem;
    @FXML
    private MenuItem signInMenuItem;
    @FXML
    private ToggleGroup gameType;

    private rmimainserver.RMIMainServer mainServerStub = null;
    private rmigameserver.RMIGameServer gameServerStub = null;
    private rmigameclient.RMIGameClient gameClientStub = null;
    private Boolean connectedToMain = false;
    private Boolean connectedToGame = false;
    private Boolean userLogged = false;
    private AlertBox alert;
    private Registry reg;


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
                                if (connectedToMain)
                                    mainServerStub.disconnect();
                                if (connectedToGame)
                                    gameServerStub.disconnect(gameClientStub);
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

    TicTacToeGameClient game = null;


    @FXML
    public void connectMenuPressed() {
        try {
            // connect to the main server
            reg = LocateRegistry.getRegistry(null, 1777);
            mainServerStub = (RMIMainServer) reg.lookup("MainServer");
            mainServerStub.connect();
            connectedToMain = true;

            // display connection successful dialog
            new AlertBox(Alert.AlertType.INFORMATION,
                    "Connection successful\nPlease Sign in/Create new user",
                    true).show();

            // update UI menu
            connectMenuItem.setDisable(true);
            createUserMenuItem.setDisable(false);
            signInMenuItem.setDisable(false);

            // todo show high score when connected to main server!
        } catch (RemoteException e) {
            e.printStackTrace();
            // display connection error msg
            AlertBox alertErr = new AlertBox(Alert.AlertType.INFORMATION,
                    "connection error",
                    true);
            alertErr.show();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void startGameClicked() {
        // retrieve selected game type
        // and connect to the corresponding remote matchmaking server
        RadioMenuItem gameSelectedButton = (RadioMenuItem) gameType.getSelectedToggle();
        String gameSelected = gameSelectedButton.getText();
        String gameServer;
        try {
            gameServer = gameSelected.replaceAll("\\s", "") + "Server";
            gameServerStub = (RMIGameServer) reg.lookup(gameServer);

            // export the object of the game thread for the server usage
            gameClientStub = (RMIGameClient) UnicastRemoteObject.exportObject(
                    game = new TicTacToeGameClient(this), 0);
            connectedToGame = true;

            // deploy game handling thread
            Thread t = new Thread(game);
            t.start();
            System.out.println("startedd thread");
        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        if (connectedToMain)
            connectMenuItem.setDisable(true);
        //create alert box
        alert = new AlertBox(Alert.AlertType.INFORMATION,
                connectedToMain ? "please wait" : "connection error",
                connectedToMain);


        //set alertBox on close action -> disconnect
        Window alertWin = alert.getDialogPane().getScene().getWindow();
        alertWin.setOnCloseRequest(windowEvent -> {
            try {
                if (connectedToMain) {
                    connectMenuItem.setDisable(false);
                    connectedToMain = false;
                    gameServerStub.disconnect(gameClientStub);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        alert.show();

        //create background thread to handle matchmaking
        //so the UI stays responsive using task object
        Task<String> connect = new Task<>() {
            @Override
            protected String call() {
                String srvAns = null;
                String[] supprtedGames;
                try {
                    srvAns = gameServerStub.connect(gameClientStub);
                    System.out.println("supported games" +
                            Arrays.toString(supprtedGames =
                                    gameServerStub.getSupportedGames()));
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

    public void exitMenuPressed() {
        try {
            if (connectedToMain)
                gameServerStub.disconnect(gameClientStub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Platform.exit();
    }


    @FXML
    public void signinMenuPressed(ActionEvent event) {
        try {
            //load login screen
            FXMLLoader fxmlLoader;
            try {
                fxmlLoader = new FXMLLoader(
                        getClass().getResource("login_window.fxml"));
                Parent root = fxmlLoader.load();
                LoginController loginController = fxmlLoader.getController();
                loginController.setMain(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startGame(Parent root) {
        Platform.runLater(() -> {
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.show();
                    System.out.println("asdasdasdasdasdasdasd");
                }
        );
    }

    public void print(String msg) {
        System.out.println("this is controller main :\n" + msg);
    }

    public void setUserLogged(boolean logged) {
        userLogged = logged;
        startGameMenu.setDisable(false);
        signInMenuItem.setDisable(true);
        createUserMenuItem.setDisable(true);
        gameTypeMenu.setDisable(false);
    }
}