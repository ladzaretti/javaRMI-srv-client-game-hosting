package rmigameserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

// interface for a matchmaking server
public interface RMIGameServer extends Remote {
    String connect(Remote gameClient) throws RemoteException;

    String[] getSupportedGames() throws RemoteException;

    void disconnect(Remote gameClient) throws RemoteException;
}
