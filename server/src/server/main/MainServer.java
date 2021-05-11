package server.main;

import hibernate.entity.User;
import javafx.application.Application;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import rmigameserver.RMIGameServer;
import rmimainserver.RMIMainServer;
import server.main.game.GameServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class MainServer extends Application implements RMIMainServer {
    private final SessionFactory factory;

    public MainServer() {
        // create session factory
        factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(User.class)
                .buildSessionFactory();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Main Server");
        stage.setOnCloseRequest(e -> factory.close());
        stage.setResizable(false);
        stage.setHeight(0);
        stage.setWidth(300);
        stage.show();
    }

    @Override
    public String connect() throws RemoteException {
        return "connected to main srv";
    }

    public synchronized void create(String user, String password) throws RemoteException {
        // create a session
        Session session = factory.getCurrentSession();

        // create a User object
        User tempUser = new User(
                user,
                password);
        // start a transaction
        session.beginTransaction();

        // save the student object
        session.save(tempUser);

        // commit transaction
        session.getTransaction().commit();
    }

    @Override
    public synchronized User read(String id) {

        // create a session
        Session session = factory.getCurrentSession();
        session.beginTransaction();

        // retrieve student based on the id: primary key
        User user = session.get(User.class, id);

        // commit the transaction
        session.getTransaction().commit();
        session.close();
        return user;
    }

    @Override
    public synchronized void setQuery(String query) {
        // create a session
        Session session = factory.getCurrentSession();
        session.beginTransaction();

        // set query
        session.createQuery(query).executeUpdate();
        session.close();

    }

    @Override
    public synchronized List<User> getQuery(String query, int maxResults) {
        // create a session
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        // get query
        List<User> list = session.createQuery(query).setMaxResults(maxResults).getResultList();
        session.close();
        return list;
    }

    @Override
    public void disconnect() throws RemoteException {

    }


    @Override
    public boolean signIn(String username, String pass) throws RemoteException {
        User user = read(username);
        if (user == null)
            return false;
        return user.getUserName().equals(username) &&
                user.getPassword().equals(pass);
    }

    @Override
    public boolean createUser(String username, String password) throws RemoteException {
        // check if user exists
        if (read(username) != null)
            return false;
        else
            create(username, password);
        return true;
    }

    public void updateSQLUser(String username, int wins, int losses, SupportedGames type) {
        User user = read(username);
        String query = "";
        int updatedWins, updatedLosses, diff;
        if (type == SupportedGames.TICTAKTOE) {
            updatedWins = user.getTttWins() + wins;
            updatedLosses = user.getTttLoses() + losses;
            diff = user.getTttDiff() + wins - losses;
            query = "UPDATE User set tttWins = " + updatedWins
                    + ", tttLosses = " + updatedLosses
                    + ", tttDiff = " + diff
                    + "where userName ='" + username + "'";
            System.out.println("query: [" + query + "]");
        } else if (type == SupportedGames.TICTAKTOERED) {
            updatedWins = user.getChWins() + wins;
            updatedLosses = user.getChLoses() + losses;
            diff = user.getChDiff() + wins - losses;
            query = "UPDATE User set tttWinsRed = " + updatedWins
                    + ", tttLossesRed = " + updatedLosses
                    + ", tttDiffRed = " + diff
                    + "where userName ='" + username + "'";
            System.out.println("query: [" + query + "]");
        } else if (type == SupportedGames.CHECKERS) {
            //
        }

        setQuery(query);
    }

    @Override
    public void ping() throws RemoteException {

    }

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            //create remote object
            BlockingMatchMaking<Remote> ticTacToe = new BlockingMatchMaking<>(2);
            BlockingMatchMaking<Remote> checkers = new BlockingMatchMaking<>(2);
            BlockingMatchMaking<Remote> ticTacToeBlack = new BlockingMatchMaking<>(2);

            MainServer srv = new MainServer();
            GameServer tttSrv = new GameServer(ticTacToe,
                    port,
                    srv,
                    SupportedGames.TICTAKTOE);

            GameServer cSrv = new GameServer(checkers,
                    port,
                    srv,
                    SupportedGames.CHECKERS);

            GameServer tttBSrv = new GameServer(ticTacToeBlack,
                    port,
                    srv,
                    SupportedGames.TICTAKTOERED);
            System.setProperty("java.rmi.server.hostname", "localhost");
            System.setProperty("rmiregistry", " -J-Djava.rmi.server.useCodebaseOnly=false");
            Registry reg = LocateRegistry.
                    createRegistry(port);


            //export tic tac toe matchmaking server
            RMIGameServer tttStub =
                    (RMIGameServer) UnicastRemoteObject.exportObject(tttSrv, port);

            reg.bind("TicTacToeServer", tttStub);

            //export tic tac toe Black matchmaking server
            RMIGameServer tttBStub =
                    (RMIGameServer) UnicastRemoteObject.exportObject(tttBSrv, port);
            reg.bind("TicTacToeRedServer", tttBStub);

            //export checkers matchmaking server
            RMIGameServer cStub =
                    (RMIGameServer) UnicastRemoteObject.exportObject(cSrv, port);
            reg.bind("CheckersServer", cStub);


            //export main server
            RMIMainServer mainStub =
                    (RMIMainServer) UnicastRemoteObject.exportObject(srv, port);
            //bind remote server to the registry
            reg.bind("MainServer", mainStub);


            System.err.println("server ready");
            launch(args);

            // cleaup
            UnicastRemoteObject.unexportObject(tttSrv, false);
            UnicastRemoteObject.unexportObject(cSrv, false);
            UnicastRemoteObject.unexportObject(reg, false);
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }
    }
}
