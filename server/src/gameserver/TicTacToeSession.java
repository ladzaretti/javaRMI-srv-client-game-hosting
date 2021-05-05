package gameserver;

import rmigameclient.RMIGameClient;
import rmigamesession.RMIGameSession;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class TicTacToeSession implements RMIGameSession {
    private RMIGameClient player1;
    private RMIGameClient player2;
    private int id1;
    private int id2;
    private boolean turnP1 = true;
    private boolean playable = true;
    private Tile[][] board;
    private final List<Combo> combos = new ArrayList<>();

    public void setPlayer1(RMIGameClient player1) {
        this.player1 = player1;
    }

    public void setPlayer2(RMIGameClient player2) {
        this.player2 = player2;
    }

    public TicTacToeSession() {
        board = new Tile[3][3];
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                board[j][i] = new Tile();
            }
        //horizontal
        for (int y = 0; y < 3; y++)
            combos.add(new Combo(
                    board[0][y],
                    board[1][y],
                    board[2][y]));
        //vertical
        for (int x = 0; x < 3; x++)
            combos.add(new Combo(
                    board[x][0],
                    board[x][1],
                    board[x][2]));
        //diagonals
        combos.add(new Combo(
                board[0][0],
                board[1][1],
                board[2][2]));
        combos.add(new Combo(
                board[2][0],
                board[1][1],
                board[0][2]));
    }

    public TicTacToeSession(RMIGameClient player1, RMIGameClient player2) {
        this();
        this.player1 = player1;
        this.id1 = player1.hashCode();

        this.player2 = player2;
        this.id2 = player2.hashCode();
    }

    @Override
    public synchronized boolean move(int x, int y, int id) throws RemoteException {
        boolean valid = false;
        // check if tile is occupied or game is playable
        if (board[y][x].getValue() != 0
                || !playable)
            return false;
        if (id == id1 && turnP1) {
            //p1 turn
            valid = true;
            player2.update(x, y);
            board[y][x].setValue(1);
            turnP1 = false;
        }
        if (id == id2 && !turnP1) {
            //p2 turn
            valid = true;
            player1.update(x, y);
            board[y][x].setValue(2);
            turnP1 = true;
        }
        checkState();
        return valid;
    }

    public void sendConnectionInfo(String msg) {
        try {
            player1.setConnectionInfo(msg, id1, "X");
            player2.setConnectionInfo(msg, id2, "O");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void checkState() {
        for (Combo combo : combos) {
            if (combo.isComplete()) {
                playable = false;
                System.out.println("someone won");
                //todo play animation using the current combo
                break;
            }
        }

    }

    class Combo {
        Tile[] tiles;

        public Combo(Tile... tiles) {
            this.tiles = tiles;
        }

        public boolean isComplete() {
            //System.out.println(tiles[0].getValue() + " " + tiles[1].getValue() + " " + tiles[2].getValue());
            if (tiles[0].getValue() == 0)
                return false;
            return tiles[0].getValue() == (tiles[1].getValue())
                    && tiles[0].getValue() == (tiles[2].getValue());
        }
    }

    class Tile {
        int value;

        public void setValue(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
