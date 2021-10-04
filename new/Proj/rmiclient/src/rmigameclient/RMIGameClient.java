package rmigameclient;

import java.rmi.Remote;
import java.rmi.RemoteException;

// remote game client interface
public interface RMIGameClient extends Remote {

    // method used by game session to set needed properties
    String setConnectionInfo(String server, int playerID, String sign) throws RemoteException;

    // used by session for board updates
    void update(int x, int y) throws RemoteException;

    // used by session to activate game over sequence
    void playGameOverRoutine(int[] start, int[] end) throws RemoteException;

    // used by session to update client regarding other player disconnection
    void opponentDisconnected() throws RemoteException;

    // show game over message
    void showGameOverMessage(boolean win, int urScore, int opScore) throws RemoteException;

    // ping client
    void ping() throws RemoteException;

    // get chat messeges from other player thought game session
    void recieveChatMessage(String msg) throws RemoteException;

    // used to get username from client
    String getUserName() throws RemoteException;
}
