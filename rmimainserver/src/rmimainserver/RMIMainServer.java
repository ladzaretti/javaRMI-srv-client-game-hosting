package rmimainserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIMainServer<T> extends Remote {
    String connect() throws RemoteException;

    void create(String user, String password) throws RemoteException;


    T read(String id) throws RemoteException;

    void setQuery(String query) throws RemoteException;

    List<T> getQuery(String query) throws RemoteException;

    void disconnect() throws RemoteException;

    void ping() throws RemoteException;
}
