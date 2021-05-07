package gameserver;

import rmigameclient.RMIGameClient;
import rmigamesession.RMIGameSession;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TicTacToeSession implements RMIGameSession {
    private RMIGameClient player1;
    private RMIGameClient player2;
    private int id1;
    private int id2;
    private int p1Score, p2Score;
    private boolean turnP1 = true;
    private boolean playable = true;
    private boolean p1Ready, p2Ready;
    private final Tile[][] board;
    private final List<Combo> combos = new ArrayList<>();
    private int moveCount;
    private final int MAXMOVE = 9;

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
        p1Score = p2Score = 0;
        this.player2 = player2;
        this.id2 = player2.hashCode();
        p1Ready = p2Ready = true;

        Thread ping = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    player1.ping();
                } catch (RemoteException e) {
                    try {
                        player2.opponentDisconnected();
                        return;
                    } catch (RemoteException remoteException) {
                        remoteException.printStackTrace();
                    }
                    e.printStackTrace();
                    return;
                }
                try {
                    player2.ping();
                } catch (RemoteException e) {
                    try {
                        player1.opponentDisconnected();
                        return;
                    } catch (RemoteException remoteException) {
                        remoteException.printStackTrace();
                    }
                    e.printStackTrace();
                    return;
                }

            }
        });
        ping.setDaemon(true);
        ping.start();
       /*ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    player1.ping();
                } catch (RemoteException e) {
                    try {
                        player2.opponentDisconnected();
                        return;
                    } catch (RemoteException remoteException) {
                        remoteException.printStackTrace();
                    }
                    e.printStackTrace();
                    return;
                }
                try {
                    player2.ping();
                } catch (RemoteException e) {
                    try {
                        player1.opponentDisconnected();
                        return;
                    } catch (RemoteException remoteException) {
                        remoteException.printStackTrace();
                    }
                    e.printStackTrace();
                    return;
                }

            }
        }, 0, 5, TimeUnit.SECONDS);*/
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
            moveCount++;
            player2.update(c, r);
            board[r][c].setValue(1);
            turnP1 = false;
        }
        if (id == id2 && !turnP1) {
            //p2 turn
            valid = true;
            moveCount++;
            player1.update(c, r);
            board[r][c].setValue(2);
            turnP1 = true;
        }
        if (checkState()) {
            int winningID;
            if (id == id1) {
                p1Score++;
                winningID = id1;
            } else {
                p2Score++;
                winningID = id2;
            }
            gameOver(winningID);
        }
        if (playable && moveCount == MAXMOVE) {
            gameOver(-1);

        }
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

    private boolean checkState() {
        for (Combo combo : combos) {
            if (combo.isComplete()) {
                int[] start = combo.tiles[0].getPos();
                int[] end = combo.tiles[2].getPos();
                try {
                    player1.playGameOverRoutine(start, end);
                    player2.playGameOverRoutine(start, end);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
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

    @Override
    public void sessionEnded(int id) throws RemoteException {
        if (id1 == id)
            player2.opponentDisconnected();
        else if (id2 == id)
            player1.opponentDisconnected();
    }

    public void gameOver(int winningID) throws RemoteException {
        player1.showGameOverMessage(id1 == winningID, p1Score, p2Score);
        player2.showGameOverMessage(id2 == winningID, p2Score, p1Score);
        reset();
    }

    public void reset() {
        for (int c = 0; c < 3; c++)
            for (int r = 0; r < 3; r++) {
                board[r][c].setValue(0);
            }
        turnP1 = true;
        moveCount = 0;
        playable = false;
        p1Ready = p2Ready = false;
    }

    @Override
    public void setPlayerReady(int id) throws RemoteException {
        if (id == id1)
            p1Ready = true;
        if (id == id2)
            p2Ready = true;
        if (p1Ready && p2Ready)
            playable = true;
    }

    @Override
    public void ping() throws RemoteException {
    }
}