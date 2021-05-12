package server.main.game;

import rmigameserver.RMIGameServer;
import rmigameclient.RMIGameClient;
import rmigamesession.RMIGameSession;
import server.main.SupportedGames;
import server.main.BlockingMatchMaking;
import server.main.MainServer;
import server.main.game.session.TicTacToeSession;
import server.main.game.session.TicTacToeSessionRed;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


// this remote class handles matchmaking using the
// given blocking queue
public class GameServer implements RMIGameServer {

    final BlockingMatchMaking<Remote> queue;
    final int port;
    final MainServer main;

    public GameServer(BlockingMatchMaking<Remote> queue,
                      int port,
                      MainServer main,
                      SupportedGames type) {
        this.queue = queue;
        this.port = port;
        this.main = main;
        // consumer thread, blocks until there are two waiting players
        // in the data structure.
        new Thread(() -> {
            ArrayList<Remote> players;
            while (true) {
                String gameSessionInfo;
                // pull waiting players
                players = queue.clear();
                RMIGameClient p1 = (RMIGameClient) players.get(0);
                RMIGameClient p2 = (RMIGameClient) players.get(1);
                RMIGameSession gameSession = null;
                // start new game session
                if (type == SupportedGames.TICTAKTOE)
                    gameSession = new TicTacToeSession(p1, p2, main);
                else if (type == SupportedGames.TICTAKTOERED)
                    gameSession = new TicTacToeSessionRed(p1, p2, main);
                else if (type == SupportedGames.CHECKERS) {
                    // todo create checkers session
                }
                try {
                    // export game session
                    RMIGameSession gameSessionStub =
                            (RMIGameSession) UnicastRemoteObject.exportObject(
                                    gameSession,
                                    port);
                    Registry reg = LocateRegistry.getRegistry(port);
                    // bind session with its hashcode
                    reg.bind(gameSessionInfo =
                            String.valueOf(gameSessionStub.hashCode()), gameSessionStub);
                    assert gameSession != null;
                    // use game session to update players with the corresponding
                    // connection info.
                    gameSession.sendConnectionInfo(gameSessionInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // used by client to connect to the matchmaking service.
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
