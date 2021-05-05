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
    private final Tile[][] board;
    private final List<Combo> combos = new ArrayList<>();

    public void setPlayer1(RMIGameClient player1) {
        this.player1 = player1;
    }

    public void setPlayer2(RMIGameClient player2) {
        this.player2 = player2;
    }

    public TicTacToeSession() {
        board = new Tile[3][3];
        for (int c = 0; c < 3; c++)
            for (int r = 0; r < 3; r++) {
                board[r][c] = new Tile(r, c);
            }
        //horizontal
        for (int c = 0; c < 3; c++)
            combos.add(new Combo(
                    board[0][c],
                    board[1][c],
                    board[2][c]));
        //vertical
        for (int r = 0; r < 3; r++)
            combos.add(new Combo(
                    board[r][0],
                    board[r][1],
                    board[r][2]));
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
    public synchronized boolean move(int c, int r, int id) throws RemoteException {
        boolean valid = false;
        // check if tile is occupied or game is playable
        if (board[r][c].getValue() != 0
                || !playable)
            return false;
        if (id == id1 && turnP1) {
            //p1 turn
            valid = true;
            player2.update(c, r);
            board[r][c].setValue(1);
            turnP1 = false;
        }
        if (id == id2 && !turnP1) {
            //p2 turn
            valid = true;
            player1.update(c, r);
            board[r][c].setValue(2);
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
                int[] start = combo.tiles[0].getPos();
                int[] end = combo.tiles[2].getPos();
                try {
                    player1.setEndGame(start, end);
                    player2.setEndGame(start, end);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                // todo show game over alert
                // todo keep score
                // todo option to reset game
                break;
            }
        }

    }

    private class Combo {
        Tile[] tiles;

        public Combo(Tile... tiles) {
            this.tiles = tiles;
        }

        public boolean isComplete() {
            if (tiles[0].getValue() == 0)
                return false;
            return tiles[0].getValue() == (tiles[1].getValue())
                    && tiles[0].getValue() == (tiles[2].getValue());
        }
    }

    private class Tile {
        int value;
        int r;
        int c;

        public Tile(int r, int c) {
            this.r = r;
            this.c = c;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public int[] getPos() {
            return new int[]{r, c};
        }
    }
}