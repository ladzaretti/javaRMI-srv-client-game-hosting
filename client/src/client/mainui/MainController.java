package client.mainui;

import UI.AlertBox;
import client.SupportedGames;
import client.game.tictactoered.TicTacToeRedGameClient;
import client.mainui.createuser.CreateUserController;
import client.mainui.highscoretableview.HSTableCtrl;
import client.mainui.login.LoginController;
import client.game.tictactoe.TicTacToeGameClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import rmigameclient.RMIGameClient;
import rmigameserver.RMIGameServer;
import rmimainserver.RMIMainServer;


// this is the main UI window controller
// controls all communication with the main server
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
    private MenuItem refreshMenuItem;
    @FXML
    private ToggleGroup gameType;
    @FXML
    private AnchorPane anchorPane;


    protected RMIMainServer getMainServerStub() {
        return mainServerStub;
    }

    private rmimainserver.RMIMainServer mainServerStub = null;
    private rmigameserver.RMIGameServer gameServerStub = null;
    private rmigameclient.RMIGameClient gameClientStub = null;
    private Boolean connectedToMain = false;
    private Boolean connectedToGame = false;
    private AlertBox alert;
    private Registry reg;
    private Stage gameStage;
    private String username;
    private HSTableCtrl hsTableCtrl;
    private SupportedGames gameTypeEnum;

    // set on close handler for the main window
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
                        // stage is set.
                        // set on close request for the main window
                        newWindow.setOnCloseRequest(windowEvent -> {
                            try {
                                // send disconnect request for remote servers
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
        // load high score table
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("highscoretableview/highscore_tableview.fxml"));
        try {
            Parent table = fxmlLoader.load();
            hsTableCtrl = fxmlLoader.getController();
            anchorPane.getChildren().add(table);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    RMIGameClient game = null;

    // used by game client class to update user name after successful login
    public String getUsername() {
        return username;
    }

    @FXML
    // connect menu press handler
    public void connectMenuPressed() {
        try {
            // connect to the main server
            reg = LocateRegistry.getRegistry(null, 1777);
            mainServerStub = (RMIMainServer) reg.lookup("MainServer");
            mainServerStub.connect();
            connectedToMain = true;
            connectMenuItem.setDisable(false);
            // load high score data
            hsTableCtrl.setSrv(mainServerStub);
            hsTableCtrl.updateHighScoreTable();
            // display connection successful dialog
            new AlertBox(Alert.AlertType.INFORMATION,
                    "Connection successful\nPlease Sign in/Create new user",
                    true).show();

            // update UI menu
            connectMenuItem.setDisable(true);
            createUserMenuItem.setDisable(false);
            signInMenuItem.setDisable(false);
            refreshMenuItem.setDisable(false);

            // start pinging thread in order to catch unexpected
            // server failure
            Thread ping = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        try {
                            mainServerStub.ping();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            // display connection error msg
                            serverIsDown("Main Server down");
                            connectedToMain = false;
                            connectedToGame = false;
                            connectMenuItem.setDisable(false);
                            startGameMenu.setDisable(true);
                            createUserMenuItem.setDisable(true);
                            signInMenuItem.setDisable(true);
                            refreshMenuItem.setDisable(true);

                            return;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            ping.setDaemon(true);
            ping.start();

            // todo fix app is hanging after closing
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

    // method used by non UI threads in order to display
    // connection error messages; mainly by game client extending classes
    public void serverIsDown(String msg) {
        Platform.runLater(() -> {
                    AlertBox alertErr = new AlertBox(Alert.AlertType.INFORMATION,
                            msg,
                            true);
                    alertErr.showAndWait();
                    if (gameStage != null)
                        gameStage.close();
                    if (alert.isShowing())
                        alert.close();
                }
        );
    }

    protected void setConnectedToGame(Boolean connectedToGame) {
        this.connectedToGame = connectedToGame;
    }

    @FXML
    // start game menu press handler
    public void startGameClicked() {
        // retrieve selected game type
        // and connect to the corresponding remote matchmaking server
        RadioMenuItem gameSelectedButton = (RadioMenuItem) gameType.getSelectedToggle();
        String gameSelected = gameSelectedButton.getText();
        String gameServer;
        try {
            // get game type chosen and set corresponding alias for connection
            // to the corresponding remote object
            gameServer = gameSelected.replaceAll("\\s", "") + "Server";
            gameServerStub = (RMIGameServer) reg.lookup(gameServer);
            // export the object of the game thread for the server usage
            if (gameServer.equals("TicTacToeServer")) {
                gameTypeEnum = SupportedGames.TICTAKTOE;
                game = new TicTacToeGameClient(this);
            } else if (gameServer.equals("TicTacToeRedServer")) {
                gameTypeEnum = SupportedGames.TICTAKTOERED;
                game = new TicTacToeRedGameClient(this);
            } else if (gameServer.equals("CheckersServer")) {
                // todo create checkers session
                gameTypeEnum = SupportedGames.CHECKERS;
            }

            // export game client.
            gameClientStub = (RMIGameClient) UnicastRemoteObject.exportObject(
                    game, 0);
            connectedToGame = true;

            // deploy game handling thread
            Thread t = new Thread((Runnable) game);
            t.start();
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
                !connectedToMain);


        //set alertBox on close action -> disconnect
        Window alertWin = alert.getDialogPane().getScene().getWindow();
        alertWin.setOnCloseRequest(windowEvent -> {
            try {
                if (connectedToMain) {
                    //connectedToMain = false;
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
                    // send remote object ref to the game server
                    // thread blocked until game match
                    srvAns = gameServerStub.connect(gameClientStub);
                    // woke
                    supprtedGames = gameServerStub.getSupportedGames();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return srvAns;
            }
        };
        connect.setOnSucceeded(e -> {
            connectionInfo = connect.getValue();

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
            if (connectedToMain && gameServerStub != null)
                gameServerStub.disconnect(gameClientStub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (game != null) {
            switch (gameTypeEnum) {
                case CHECKERS -> {
                    // todo add later
                }
                case TICTAKTOE -> {
                    ((TicTacToeGameClient) game).disconnect();
                }
                case TICTAKTOERED -> {
                    ((TicTacToeRedGameClient) game).disconnect();
                }
            }
        }
        Platform.exit();
    }


    @FXML
    // sign in menu pressed
    public void signinMenuPressed(ActionEvent event) {
        try {
            //load login screen
            FXMLLoader fxmlLoader;
            try {
                fxmlLoader = new FXMLLoader(
                        getClass().getResource("login/login_window.fxml"));
                Parent root = fxmlLoader.load();
                LoginController loginController = fxmlLoader.getController();
                loginController.setMain(this);
                loginController.setSrv(mainServerStub);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Login");
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @FXML
    public void createUserMenuPressed(ActionEvent event) {
        try {
            //load create user screen
            FXMLLoader fxmlLoader;
            try {
                fxmlLoader = new FXMLLoader(
                        getClass().getResource("createuser/create_user_window.fxml"));
                Parent root = fxmlLoader.load();
                CreateUserController createUserController = fxmlLoader.getController();
                createUserController.setMain(this);
                createUserController.setSrv(mainServerStub);
                Stage stage = new Stage();
                stage.setTitle("Create New User");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method used by main controller to start a given game.
    // ie display UI for the game session, as the game client thread is not
    // the UI owner thread.
    public void startGame(Parent root) {
        Platform.runLater(() -> {
                    gameStage = new Stage();
                    gameStage.setTitle("Game Session: " + username);
                    gameStage.setScene(new Scene(root));
                    gameStage.setOnCloseRequest(e ->
                    {
                        System.out.println("closed");
                        switch (gameTypeEnum) {
                            case CHECKERS -> {
                                // todo add later
                            }
                            case TICTAKTOE -> {
                                ((TicTacToeGameClient) game).disconnect();
                            }
                            case TICTAKTOERED -> {
                                ((TicTacToeRedGameClient) game).disconnect();
                            }
                        }
                    });
                    gameStage.setResizable(false);
                    gameStage.show();
                }
        );
    }

    private void displayNewGenericMessage(String msg, boolean show) {
        Platform.runLater(() -> {
                    AlertBox gameOverDialog =
                            new AlertBox(Alert.AlertType.INFORMATION, msg
                                    , true);
                    gameOverDialog.showAndWait();
                }
        );
    }


    // used by game client op pinging thread to display msges
    public void opponentDisconnectedAlert() {
        Platform.runLater(() -> {
                    AlertBox opponnetDisDialog =
                            new AlertBox(Alert.AlertType.INFORMATION, "opponent disconnected"
                                    , true);
                    opponnetDisDialog.showAndWait();
                    gameStage.close();

                }
        );
        //displayNewGenericMessage("opponent disconnected", true);
    }


    // used by game client to display game over/winning messages
    public void playerWonAlert(String msg) {
        Platform.runLater(() -> {
            AlertBox gameOverDialog =
                    new AlertBox(Alert.AlertType.INFORMATION, msg
                            , true);
            gameOverDialog.showAndWait();
            switch (gameTypeEnum) {
                case CHECKERS -> {
                    // todo add later
                }
                case TICTAKTOE -> {
                    ((TicTacToeGameClient) game).setPlayerReady();
                    ((TicTacToeGameClient) game).resetBoard();
                }
                case TICTAKTOERED -> {
                    ((TicTacToeRedGameClient) game).setPlayerReady();
                    ((TicTacToeRedGameClient) game).resetBoard();
                }
            }


        });
        //displayNewGenericMessage(msg, true);
    }

    @FXML
    public void aboutMenuPressed() {
        displayNewGenericMessage("This App is part of an assignment " +
                "\nin Java workshop at the Open University of israel." +
                "\nAuthor: Ladzaretti Gabriel", true);
    }

    @FXML
    // refresh hs table from menu
    public void refreshMenuPressed() {
        hsTableCtrl.updateHighScoreTable();
    }

    public void setUserLogged(boolean logged) {
        //userLogged = logged;
        startGameMenu.setDisable(false);
        signInMenuItem.setDisable(true);
        createUserMenuItem.setDisable(true);
        gameTypeMenu.setDisable(false);
        refreshMenuItem.setDisable(false);
    }
}