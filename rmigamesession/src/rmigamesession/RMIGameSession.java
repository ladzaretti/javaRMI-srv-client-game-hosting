package rmigamesession;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIGameSession extends Remote {
    String move(String request) throws RemoteException;
}
