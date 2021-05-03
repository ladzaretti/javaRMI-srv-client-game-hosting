package rmigameserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIGameServer extends Remote {
    String connect(Remote gameClient) throws RemoteException;

    String[] getSupportedGames() throws RemoteException;

    void disconnect(Remote gameClient) throws RemoteException;
}