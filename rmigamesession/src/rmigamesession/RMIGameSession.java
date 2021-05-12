package rmigamesession;

import java.rmi.Remote;
import java.rmi.RemoteException;


// game session remote object. manges 2 players playing a
// given game.
public interface RMIGameSession extends Remote {
    // method used by players for turn/move register
    boolean move(int x, int y, int id) throws RemoteException;

    // used to notify game session server when disconnecting
    void sessionEnded(int id) throws RemoteException;

    // used by users to notify readiness for a new round
    void setPlayerReady(int id) throws RemoteException;

    // used to ping server and check connection
    void ping() throws RemoteException;

    // send chat msg
    void sendChatMessage(int id, String msg) throws RemoteException;

    // used by match making server to notify players regarding
    // connection info
    void sendConnectionInfo(String msg) throws RemoteException;

}
