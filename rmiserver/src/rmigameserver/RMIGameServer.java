package rmigameserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIGameServer extends Remote {
    String connect() throws RemoteException;

    void disconnect() throws RemoteException;
}
