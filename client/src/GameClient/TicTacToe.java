package GameClient;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import rmigamesession.RMIGameSession;

import java.rmi.RemoteException;


public class TicTacToe {
    private final static int RECTSIZE = 100;
    private final static int BOARDSIZE = 300;
    private final Tile[][] board = new Tile[3][3];
    private final Pane root = new Pane();
    private final RMIGameSession srv;
    private final int playerID;
    private final String sign;
    private final String opSign;

    public TicTacToe(RMIGameSession srv, int id, String sign) {
        this.srv = srv;
        this.playerID = id;
        this.sign = sign;
        this.opSign = sign.equals("X") ? "O" : "X";
    }

    public void drawOpponent(int x, int y) {
        board[y][x].text.setText(opSign);
    }

    public void playWinAnimation(int[] start, int[] end) {
        Platform.runLater(() ->
                {
                    Line line = new Line();
                    Tile startTile = board[start[0]][start[1]];
                    Tile endTile = board[end[0]][end[1]];
                    line.setStartX(startTile.getCenterX());
                    line.setStartY(startTile.getCenterY());
                    line.setEndX(startTile.getCenterX());
                    line.setEndY(startTile.getCenterY());

                    root.getChildren().add(line);
                    Timeline timeline = new Timeline();
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1),
                            new KeyValue(line.endXProperty(), endTile.getCenterX()),
                            new KeyValue(line.endYProperty(), endTile.getCenterY()))
                    );
                    timeline.play();
                }
        );

    }

    public Parent createContent() {
        root.setPrefSize(BOARDSIZE, BOARDSIZE);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                Tile tile = new Tile(i, j);
                tile.setTranslateX(j * RECTSIZE);
                tile.setTranslateY(i * RECTSIZE);

                root.getChildren().add(tile);

                board[j][i] = tile;
            }
        return root;
    }


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

            setOnMouseClicked(event -> {
                try {
                    if (srv.move(x, y, playerID)) {
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

        public double getCenterX() {
            return getTranslateX() + (double) RECTSIZE / 2;
        }

        public double getCenterY() {
            return getTranslateY() + (double) RECTSIZE / 2;
        }

    }
}
