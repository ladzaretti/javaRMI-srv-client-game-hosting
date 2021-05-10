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

    boolean signIn(String user, String password) throws RemoteException;

    boolean createUser(String user, String password) throws RemoteException;

    void updateSQLUser(int win, int loss) throws RemoteException;

    void ping() throws RemoteException;
}
