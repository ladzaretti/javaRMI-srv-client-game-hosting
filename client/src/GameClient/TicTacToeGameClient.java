package GameClient;

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
    private int id;
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
    public void setEndGame(int[] start, int[] end) throws RemoteException {
        ticTacToe.playWinAnimation(start, end);
    }

    @Override
    public void disconnect() throws RemoteException {

    }
}
