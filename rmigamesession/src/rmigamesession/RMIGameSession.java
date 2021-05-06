package rmigamesession;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIGameSession extends Remote {
    boolean move(int x, int y, int id) throws RemoteException;

    void sessionEnded(int id) throws RemoteException;

    void setPlayerReady(int id) throws RemoteException;
}
