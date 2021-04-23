package gameserver;

import rmigameserver.RMIGameServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ThreadLocalRandom;


public class GameServer implements RMIGameServer {
    BlockingMatchMaking<String> queue;
    int currentTicket;

    public GameServer(BlockingMatchMaking<String> queue) {
        this.queue = queue;
        new Thread(() -> {
            while (true) {
                currentTicket = ThreadLocalRandom.current().nextInt();
                String idString = String.valueOf(currentTicket);
                queue.fill(idString);
                System.out.println("fill");
            }
        }).start();
    }

    @Override
    public synchronized String connect() {
        String ticket = queue.take();
        System.out.println(ticket);
        return ticket + Thread.currentThread().toString();
    }

    @Override
    public synchronized void disconnect() throws RemoteException {
        System.out.println("disconnected thread: " + Thread.currentThread().toString());
    }

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            //create remote object
            BlockingMatchMaking<String> game = new BlockingMatchMaking<>(2);
            GameServer srv = new GameServer(game);

            //export the remote object
            System.setProperty("java.rmi.server.hostname", "localhost");
            RMIGameServer stub = (RMIGameServer) UnicastRemoteObject.exportObject(srv, port);

            //bind remote server to the registry
            Registry reg = LocateRegistry.
                    createRegistry(port);
            reg.bind("GameServer", stub);

            System.err.println("server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
