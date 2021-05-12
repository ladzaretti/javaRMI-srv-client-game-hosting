package client.game.tictactoe;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import rmigamesession.RMIGameSession;

import java.rmi.RemoteException;

// the following class creates the necessary ui elements
// for the tic tac toe game.
public class TicTacToe {
    private final static int RECTSIZE = 100;
    private final static int BOARDSIZE = 300;
    private final Tile[][] board = new Tile[3][3];
    private final HBox root = new HBox();

    // holds the gamesession remote object
    private final RMIGameSession srv;
    // properties given by the gamesession manger
    private final int playerID;
    private final String sign;
    private final String opSign;
    // UI elements
    private Line line;
    private TextArea chatScreen;
    private TextField chatMsg;
    private Button sendButton;
    private Pane pane;

    public TicTacToe(RMIGameSession srv, int id, String sign) {
        this.srv = srv;
        this.playerID = id;
        this.sign = sign;
        this.opSign = sign.equals("X") ? "O" : "X";
    }

    // most basic game UI element. extends stack pane, contains
    // a rectangular bordar and a text object
    private class Tile extends StackPane {
        private Text text = new Text();
        private int x;
        private int y;

        public Tile(int x, int y) {
            this.x = x;
            this.y = y;
            Rectangle border = new Rectangle(RECTSIZE, RECTSIZE);
            border.setFill(null);
            border.setStroke(Color.BLACK);
            text.setFont(Font.font(72));
            setAlignment(Pos.CENTER);
            getChildren().addAll(border, text);

            // set event handler for mouse clicking.
            setOnMouseClicked(event -> {
                try {
                    // register move to game session manager
                    if (srv.move(x, y, playerID)) {
                        // move accepeted-> draw
                        draw();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }


        private void draw() {
            text.setText(sign);
        }

        private void clear() {
            text.setText("");
        }


        public double getCenterX() {
            return getTranslateX() + (double) RECTSIZE / 2;
        }

        public double getCenterY() {
            return getTranslateY() + (double) RECTSIZE / 2;
        }

    }

    // creates gameboard UI
    public Parent createContent() {
        pane = new Pane();

        pane.setPrefSize(BOARDSIZE, BOARDSIZE);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                Tile tile = new Tile(i, j);
                tile.setTranslateX(j * RECTSIZE);
                tile.setTranslateY(i * RECTSIZE);
                pane.getChildren().add(tile);
                board[j][i] = tile;
            }
        VBox vbox = new VBox();
        vbox.setPrefSize(BOARDSIZE, BOARDSIZE);
        chatScreen = new TextArea();
        chatScreen.setWrapText(true);
        chatScreen.setEditable(false);
        chatScreen.setPrefSize(BOARDSIZE, BOARDSIZE * 0.92);
        vbox.getChildren().add(chatScreen);
        HBox hboxSend = new HBox();
        chatMsg = new TextField();
        chatMsg.setPrefSize(BOARDSIZE * 0.75, BOARDSIZE * 0.08);
        sendButton = new Button("SEND");
        sendButton.setPrefSize(BOARDSIZE * 0.25, BOARDSIZE * 0.08);

        // send button event handler
        sendButton.setOnAction(e -> {
            String msg = chatMsg.getText();
            if (msg.length() > 0) {
                try {
                    // update own chat screen
                    chatScreen.appendText("you: " + msg + "\n");
                    // send msg to gamesession manager
                    srv.sendChatMessage(playerID, msg + "\n");
                } catch (RemoteException remoteException) {
                    remoteException.printStackTrace();
                }
            }
            chatMsg.clear();
        });
        hboxSend.getChildren().addAll(chatMsg, sendButton);
        vbox.getChildren().add(hboxSend);
        root.getChildren().addAll(pane, vbox);
        return root;
    }

    public void drawOpponent(int x, int y) {
        board[y][x].text.setText(opSign);
    }

    // plays winning animation
    public void playWinAnimation(int[] start, int[] end) {
        Platform.runLater(() ->
                {
                    line = new Line();
                    Tile startTile = board[start[0]][start[1]];
                    Tile endTile = board[end[0]][end[1]];
                    line.setStartX(startTile.getCenterX());
                    line.setStartY(startTile.getCenterY());
                    line.setEndX(startTile.getCenterX());
                    line.setEndY(startTile.getCenterY());

                    pane.getChildren().add(line);
                    Timeline timeline = new Timeline();
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1),
                            new KeyValue(line.endXProperty(), endTile.getCenterX()),
                            new KeyValue(line.endYProperty(), endTile.getCenterY()))
                    );
                    timeline.play();
                }
        );
    }

    public void updateChat(String msg) {
        chatScreen.appendText(msg);
    }

    // reset gameboard. ie clear player's sign
    public void resetBoard() {
        pane.getChildren().remove(line);
        for (int c = 0; c < 3; c++)
            for (int r = 0; r < 3; r++) {
                board[r][c].clear();
            }
    }
}
