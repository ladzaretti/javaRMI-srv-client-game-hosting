package gameserver;

import rmigameserver.RMIGameServer;
import rmigameclient.RMIGameClient;
import rmigamesession.RMIGameSession;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class GameServer implements RMIGameServer {


    BlockingMatchMaking<Remote> queue;
    int port;

    public GameServer(BlockingMatchMaking<Remote> queue, int port) {
        this.queue = queue;
        this.port = port;
        new Thread(() -> {
            ArrayList<Remote> players;
            while (true) {
                String gameSessionInfo;
                players = queue.clear();
                RMIGameClient p1 = (RMIGameClient) players.get(0);
                RMIGameClient p2 = (RMIGameClient) players.get(1);
                GameSession gameSession = new GameSession(p1, p2);

                try {
                    RMIGameSession gameSessionStub =
                            (RMIGameSession) UnicastRemoteObject.exportObject(gameSession, port);
                    Registry reg = LocateRegistry.getRegistry(port);
                    reg.bind(gameSessionInfo =
                            String.valueOf(gameSessionStub.hashCode()), gameSessionStub);
                    gameSession.notifyPlayers("game Started at :" + gameSessionInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public synchronized String connect(Remote client) throws RemoteException {

        System.out.println("client connected [" + client + "]");
        queue.put(client);
        return Thread.currentThread().toString();
    }

    @Override
    public String[] getSupportedGames() throws RemoteException {
        return SupportedGames.games();
    }

    @Override
    public synchronized void disconnect(Remote client) throws RemoteException {
        queue.remove(client);
        System.out.println("disconnected thread: " + Thread.currentThread());
    }
}
