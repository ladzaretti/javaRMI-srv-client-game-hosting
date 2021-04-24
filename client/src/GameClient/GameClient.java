package GameClient;

import java.rmi.RemoteException;

public class GameClient implements rmigameclient.RMIGameClient {
    public GameClient() {
        System.out.println("Game Started");
    }

    @Override
    public String update(String srvMsg) throws RemoteException {
        System.out.println(srvMsg);
        return srvMsg;
    }

    @Override
    public void disconnect() throws RemoteException {

    }
}
