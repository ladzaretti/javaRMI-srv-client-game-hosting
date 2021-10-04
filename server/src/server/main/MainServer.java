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


// this class implements the mainserver remote interface.
// this class initializes the different matchmaking services
// and handles all communication with the mysql database.
public class MainServer extends Application implements RMIMainServer {
    private final SessionFactory factory;

    public MainServer() {
        // create hibernate session factory
        factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(User.class)
                .buildSessionFactory();
    }

    // init primitive server UI
    @Override
    public void start(Stage stage) {
        stage.setTitle("Main Server");
        stage.setOnCloseRequest(e -> {
            factory.close();
            System.exit(0);
        });
        stage.setResizable(false);
        stage.setHeight(0);
        stage.setWidth(300);
        stage.show();
    }


    // REMOTE
    @Override
    public String connect() throws RemoteException {
        return "connected to main srv";
    }

    // create new data in the mysql db.
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

    // REMOTE
    // crud methods to be used with the database:
    // read and queries.
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


    // set query on the database. used to manipulate the data
    // in the mysql db
    @Override
    public synchronized void setQuery(String query) {
        // create a session
        Session session = factory.getCurrentSession();
        session.beginTransaction();

        // set query
        session.createQuery(query).executeUpdate();
        session.close();

    }

    // get query from mysql db.
    // returns a list of User class instances
    @Override
    public synchronized List<User> getQuery(String query, int maxResults) {
        // create a session
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        // get query
        List<User> list;
        if (maxResults == -1)
            list = session.createQuery(query).getResultList();

        else
            list = session.createQuery(query).setMaxResults(maxResults).getResultList();
        session.close();
        return list;
    }

    @Override
    public void disconnect() throws RemoteException {

    }

    // REMOTE
    // used to sign clients. iff username and passwords checks out
    // in the db.
    @Override
    public boolean signIn(String username, String pass) throws RemoteException {
        User user = read(username);
        if (user == null)
            return false;
        return user.getUserName().equals(username) &&
                user.getPassword().equals(pass);
    }

    // REMOTE
    // create new user in the db
    @Override
    public boolean createUser(String username, String password) throws RemoteException {
        // check if user exists
        if (read(username) != null)
            return false;
        else
            create(username, password);
        return true;
    }

    // update db using updated data given from game session
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
            updatedWins = user.getTttWinsRed() + wins;
            updatedLosses = user.getTttLossesRed() + losses;
            diff = user.getTttDiffRed() + wins - losses;
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

    // REMOTE
    // check connection remote method.
    @Override
    public void ping() throws RemoteException {

    }

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            // create remote object for handling matchmaking for
            // the different supported game types
            BlockingMatchMaking<Remote> ticTacToe = new BlockingMatchMaking<>(2);
            BlockingMatchMaking<Remote> checkers = new BlockingMatchMaking<>(2);
            BlockingMatchMaking<Remote> ticTacToeBlack = new BlockingMatchMaking<>(2);

            // create local reg
            System.setProperty("java.rmi.server.hostname", "localhost");
            System.setProperty("rmiregistry", " -J-Djava.rmi.server.useCodebaseOnly=false");
            Registry reg = LocateRegistry.
                    createRegistry(port);


            // create main server
            MainServer srv = new MainServer();
            //export main server
            RMIMainServer mainStub =
                    (RMIMainServer) UnicastRemoteObject.exportObject(srv, port);
            //bind remote server to the registry
            reg.bind("MainServer", mainStub);


            // create TicTacToeServer gameserver
            GameServer tttSrv = new GameServer(ticTacToe,
                    port,
                    srv,
                    SupportedGames.TICTAKTOE);
            //export TicTacToeServer matchmaking server
            RMIGameServer tttStub =
                    (RMIGameServer) UnicastRemoteObject.exportObject(tttSrv, port);

            reg.bind("TicTacToeServer", tttStub);


            // create CheckersServer gameserver
            GameServer cSrv = new GameServer(checkers,
                    port,
                    srv,
                    SupportedGames.CHECKERS);
            //export checkers matchmaking server
            RMIGameServer cStub =
                    (RMIGameServer) UnicastRemoteObject.exportObject(cSrv, port);
            reg.bind("CheckersServer", cStub);

            // create TicTacToeRedServer gameserver
            GameServer tttBSrv = new GameServer(ticTacToeBlack,
                    port,
                    srv,
                    SupportedGames.TICTAKTOERED);
            //export TicTacToeRedServer matchmaking server
            RMIGameServer tttBStub =
                    (RMIGameServer) UnicastRemoteObject.exportObject(tttBSrv, port);
            reg.bind("TicTacToeRedServer", tttBStub);


            // main server ready!
            System.err.println("server ready");
            launch(args);

            // cleanup after srv UI closes
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
