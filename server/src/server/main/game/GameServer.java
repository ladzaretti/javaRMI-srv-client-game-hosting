package server.main.game;

import rmigameserver.RMIGameServer;
import rmigameclient.RMIGameClient;
import rmigamesession.RMIGameSession;
import server.main.SupportedGames;
import server.main.BlockingMatchMaking;
import server.main.MainServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class GameServer implements RMIGameServer {

    BlockingMatchMaking<Remote> queue;
    int port;
    MainServer main;

    public GameServer(BlockingMatchMaking<Remote> queue, int port, MainServer main) {
        this.queue = queue;
        this.port = port;
        this.main = main;
        new Thread(() -> {
            ArrayList<Remote> players;
            while (true) {
                String gameSessionInfo;
                players = queue.clear();
                RMIGameClient p1 = (RMIGameClient) players.get(0);
                RMIGameClient p2 = (RMIGameClient) players.get(1);
                TicTacToeSession gameSession = new TicTacToeSession(p1, p2, main);

                try {
                    RMIGameSession gameSessionStub =
                            (RMIGameSession) UnicastRemoteObject.exportObject(gameSession, port);
                    Registry reg = LocateRegistry.getRegistry(port);
                    reg.bind(gameSessionInfo =
                            String.valueOf(gameSessionStub.hashCode()), gameSessionStub);
                    gameSession.sendConnectionInfo(gameSessionInfo);
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
