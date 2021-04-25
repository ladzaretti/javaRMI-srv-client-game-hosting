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
            while (!started) {
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
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
