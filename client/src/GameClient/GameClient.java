package GameClient;

import java.rmi.RemoteException;

public class GameClient implements Runnable, rmigameclient.RMIGameClient {

    MainController cntl;
    boolean started = false;
    final Object lock;

    public GameClient(MainController cntl) {
        this.cntl = cntl;
        lock = new Object();
    }

    @Override
    public void run() {
        while (true) {
            // wait for a game match
            // then close waiting box.
            while (!started) {
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                    // closing alert box using runLater on the UI thread
                    cntl.closeWaitingBox();
                    System.out.println("started");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String update(String srvMsg) throws RemoteException {
        // server uses this method to update the client on successful gamematch
        synchronized (lock) {
            started = true;
            lock.notifyAll();
        }
        return srvMsg;
    }

    @Override
    public void disconnect() throws RemoteException {

    }
}
