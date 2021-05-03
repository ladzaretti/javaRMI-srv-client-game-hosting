package gameserver;

import rmigameserver.RMIGameServer;
import rmimainserver.RMIMainServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MainServer implements RMIMainServer {
    @Override
    public String connect() throws RemoteException {
        return "connected to main srv";
    }

    @Override
    public String[] create() throws RemoteException {
        return new String[0];
    }

    @Override
    public String[] update() throws RemoteException {
        return new String[0];
    }

    @Override
    public String[] read() throws RemoteException {
        return new String[0];
    }

    @Override
    public String[] delete() throws RemoteException {
        return new String[0];
    }

    @Override
    public void disconnect() throws RemoteException {

    }

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            //create remote object
            BlockingMatchMaking<Remote> ticTacToe = new BlockingMatchMaking<>(2);
            BlockingMatchMaking<Remote> checkers = new BlockingMatchMaking<>(2);

            MainServer srv = new MainServer();
            GameServer tttSrv = new GameServer(ticTacToe, port);
            GameServer cSrv = new GameServer(checkers, port);


            //export the remote object
            System.setProperty("java.rmi.server.hostname", "localhost");
            Registry reg = LocateRegistry.
                    createRegistry(port);


            //export tic tac toe matchmaking server
            RMIGameServer tttStub =
                    (RMIGameServer) UnicastRemoteObject.exportObject(tttSrv, port);

            reg.bind("ticServer", tttStub);


            //export checkers matchmaking server
            RMIGameServer cStub =
                    (RMIGameServer) UnicastRemoteObject.exportObject(cSrv, port);
            reg.bind("chServer", cStub);


            //export main server
            RMIMainServer mainStub =
                    (RMIMainServer) UnicastRemoteObject.exportObject(srv, port);
            //bind remote server to the registry
            reg.bind("MainServer", mainStub);

            System.err.println("server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }
    }
}
