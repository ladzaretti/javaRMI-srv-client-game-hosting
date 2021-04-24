package rmigameclient;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIGameClient extends Remote {
    String update(String msg) throws RemoteException;

    void disconnect() throws RemoteException;
}
