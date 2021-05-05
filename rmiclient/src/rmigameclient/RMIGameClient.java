package rmigameclient;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIGameClient extends Remote {
    String setConnectionInfo(String server, int playerID, String sign) throws RemoteException;

    void update(int x, int y) throws RemoteException;

    void setEndGame(int[] start, int[] end) throws RemoteException;

    void disconnect() throws RemoteException;
}
