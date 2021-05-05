package GameClient;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import java.util.ArrayList;
import java.util.List;


public class TicTacToe {
    private final static int RECTSIZE = 100;
    private final static int BOARDSIZE = 300;
    private boolean playable = true;
    private final Tile[][] board = new Tile[3][3];
    private final List<Combo> combos = new ArrayList<>();
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

    private void playWinAnimation(Combo combo) {
        Line line = new Line();
        line.setStartX(combo.tiles[0].getCenterX());
        line.setStartY(combo.tiles[0].getCenterY());
        line.setEndX(combo.tiles[0].getCenterX());
        line.setEndY(combo.tiles[0].getCenterY());

        root.getChildren().add(line);
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1),
                new KeyValue(line.endXProperty(), combo.tiles[2].getCenterX()),
                new KeyValue(line.endYProperty(), combo.tiles[2].getCenterY()))
        );
        timeline.play();
    }

    class Combo {
        Tile[] tiles;

        public Combo(Tile... tiles) {
            this.tiles = tiles;
        }

        public boolean isComplete() {
            if (tiles[0].getValue().isEmpty())
                return false;
            return tiles[0].getValue().equals(tiles[1].getValue())
                    && tiles[0].getValue().equals(tiles[2].getValue());
        }
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


    class Tile extends StackPane {
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

        private boolean isOccupied() {
            return !this.getValue().equals("");
        }

        public String getValue() {
            return text.getText();
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
