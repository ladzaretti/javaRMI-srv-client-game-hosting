package hibernate.entity;


import javax.persistence.*;
import java.io.Serializable;


// the following class is used as a representation of the corresponding
// SQL table data rows
// userName is the primary key.
@Entity
@Table(name = "user_db")
public class User implements Serializable {

    @Id
    @Column(name = "user_name")
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "ttt_losses")
    private int tttLosses;

    @Column(name = "ttt_wins")
    private int tttWins;

    @Column(name = "ttt_diff")
    private int tttDiff;

    @Column(name = "ttt_red_losses")
    private int tttLossesRed;

    @Column(name = "ttt_red_wins")
    private int tttWinsRed;

    @Column(name = "ttt_red_diff")
    private int tttDiffRed;


    @Column(name = "ch_losses")
    private int chLosses;

    @Column(name = "ch_wins")
    private int chWins;

    @Column(name = "ch_diff")
    private int chDiff;

    @Version
    private int version;

    public User() {
    }

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTttLoses() {
        return tttLosses;
    }

    public void setTttLoses(int tttLoses) {
        this.tttLosses = tttLoses;
    }

    public int getTttWins() {
        return tttWins;
    }

    public void setTttWins(int tttWins) {
        this.tttWins = tttWins;
    }

    public int getTttDiff() {
        return tttDiff;
    }

    public void setTttDiff(int tttDiff) {
        this.tttDiff = tttDiff;
    }

    public int getChLoses() {
        return chLosses;
    }

    public void setChLoses(int chLoses) {
        this.chLosses = chLoses;
    }

    public int getChWins() {
        return chWins;
    }

    public void setChWins(int chWins) {
        this.chWins = chWins;
    }

    public int getChDiff() {
        return chDiff;
    }

    public void setChDiff(int chDiff) {
        this.chDiff = chDiff;
    }

    public int getTttLossesRed() {
        return tttLossesRed;
    }

    public void setTttLossesRed(int tttLossesRed) {
        this.tttLossesRed = tttLossesRed;
    }

    public int getTttWinsRed() {
        return tttWinsRed;
    }

    public void setTttWinsRed(int tttWinsRed) {
        this.tttWinsRed = tttWinsRed;
    }

    public int getTttDiffRed() {
        return tttDiffRed;
    }

    public void setTttDiffRed(int tttDiffRed) {
        this.tttDiffRed = tttDiffRed;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", tttLoses=" + tttLosses +
                ", tttWins=" + tttWins +
                ", tttDiff=" + tttDiff +
                ", chLoses=" + chLosses +
                ", chWins=" + chWins +
                ", chDiff=" + chDiff +
                '}';
    }
}