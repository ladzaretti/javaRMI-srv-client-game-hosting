package client.game.tictactoe;

import client.mainui.MainController;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class TicTacToeGameClient implements Runnable, rmigameclient.RMIGameClient {

    private final MainController mainController;
    private boolean started = false;
    private final Object lock;
    private rmigamesession.RMIGameSession gameSession = null;
    private Boolean connected = false;
    // id given by game session server
    private int id;
    // game sign given by session
    private String sign;
    private TicTacToe ticTacToe;

    public TicTacToeGameClient(MainController cntl) {
        this.mainController = cntl;
        lock = new Object();
    }

    @Override
    public void run() {
        // wait for a game match
        // then close waiting box.
        while (!started) {
            try {
                synchronized (lock) {
                    lock.wait();
                }
                // closing alert box using runLater on the UI thread
                mainController.closeWaitingBox();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (!connected) {
            try {
                synchronized (lock) {
                    lock.wait();
                }
                mainController.startGame((ticTacToe = new TicTacToe(gameSession, id, sign)).createContent());
                //ticTacToe.setOnCloseRequest();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public String setConnectionInfo(String srv, int id, String sign) throws RemoteException {
        // server uses this method to update the client on successful gamematch
        synchronized (lock) {
            started = true;
            lock.notifyAll();
        }
        Registry reg = LocateRegistry.getRegistry(null, 1777);
        try {
            gameSession =
                    (rmigamesession.RMIGameSession) reg.lookup(srv);
            connected = true;
            this.id = id;
            this.sign = sign;
            Thread ping = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        try {
                            gameSession.ping();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            // display connection error msg
                            mainController.serverIsDown("Game Session server down");
                            connected = false;
                            //mainController.setConnectedToGame(false);
                            return;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            ping.setDaemon(true);
            ping.start();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        synchronized (lock) {
            connected = true;
            lock.notifyAll();
        }
        return srv;
    }

    @Override
    public void update(int x, int y) throws RemoteException {
        ticTacToe.drawOpponent(x, y);
    }


    @Override
    public void recieveChatMessage(String msg) {
        ticTacToe.updateChat(msg);
    }

    @Override
    public void playGameOverRoutine(int[] start, int[] end) throws RemoteException {
        ticTacToe.playWinAnimation(start, end);
    }

    public void disconnect() {
        try {
            gameSession.sessionEnded(id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void opponentDisconnected() throws RemoteException {
        System.out.println("other player gone");
        mainController.opponentDisconnectedAlert();
    }

    public void resetBoard() {
        ticTacToe.resetBoard();
    }

    public void setPlayerReady() {
        try {
            gameSession.setPlayerReady(id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showGameOverMessage(boolean win, int urScore, int opScore) throws RemoteException {
        mainController.playerWonAlert((win ? "You won!" : "Game Over") +
                "\nScore\nyou :" + urScore + "\nOpponent :" + opScore);
    }

    @Override
    public String getUserName() throws RemoteException {
        return mainController.getUsername();
    }

    @Override
    public void ping() throws RemoteException {
    }
}