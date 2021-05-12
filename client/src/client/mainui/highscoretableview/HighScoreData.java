package client.mainui.highscoretableview;

import javafx.beans.property.SimpleStringProperty;


// class to be used as data entry for the highscore table (tableview)
// on main ui window.
public class HighScoreData {
    private SimpleStringProperty user;
    private int wins, losses, diff;

    public HighScoreData(String user,
                         int wins,
                         int losses,
                         int diff) {
        this.user = new SimpleStringProperty(user);
        this.wins = wins;
        this.losses = losses;
        this.diff = diff;
    }

    public String getUser() {
        return user.get();
    }

    public SimpleStringProperty userProperty() {
        return user;
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }
}
