package gameserver;

import rmigameclient.RMIGameClient;
import rmigamesession.RMIGameSession;

import java.rmi.RemoteException;

public class GameSession implements RMIGameSession {
    RMIGameClient player1;
    RMIGameClient player2;

    public void setPlayer1(RMIGameClient player1) {
        this.player1 = player1;
    }

    public void setPlayer2(RMIGameClient player2) {
        this.player2 = player2;
    }

    public GameSession() {
    }

    public GameSession(RMIGameClient player1, RMIGameClient player2) {
        this();
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    public String move(String s) throws RemoteException {
        return null;
    }

    public void notifyPlayers(String msg) {
        try {
            player1.update(msg);
            player2.update(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
