package rmimainserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIMainServer extends Remote {
    String connect() throws RemoteException;

    String[] create() throws RemoteException;

    String[] update() throws RemoteException;

    String[] read() throws RemoteException;

    String[] delete() throws RemoteException;

    void disconnect() throws RemoteException;

    void ping() throws RemoteException;
}
