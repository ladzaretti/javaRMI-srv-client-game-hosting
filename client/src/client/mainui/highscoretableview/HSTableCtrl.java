package client.mainui.highscoretableview;

import client.mainui.MainController;
import hibernate.entity.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import rmimainserver.RMIMainServer;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ResourceBundle;

public class HSTableCtrl extends TableView<HighScoreData> implements Initializable {
    @FXML
    private TableView<HighScoreData> tableView;
    @FXML
    private TableColumn<HighScoreData, String> usernameColumn;
    @FXML
    private TableColumn<HighScoreData, Integer> diffColumn;
    @FXML
    private TableColumn<HighScoreData, Integer> winsColumn;
    @FXML
    private TableColumn<HighScoreData, Integer> lossesColumn;
    private RMIMainServer srv;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<HighScoreData, String>("user"));
        diffColumn.setCellValueFactory(new PropertyValueFactory<HighScoreData, Integer>("diff"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<HighScoreData, Integer>("wins"));
        lossesColumn.setCellValueFactory(new PropertyValueFactory<HighScoreData, Integer>("losses"));

        tableView.setEditable(false);
    }

    /*private ObservableList<HighScoreData> getData() {
        ObservableList<HighScoreData> scores = FXCollections.observableArrayList();

        scores.add(new HighScoreData("asdasd", 10, 5, 1));
        scores.add(new HighScoreData("asdaxcvbcxsd", 10, 5, 3));
        scores.add(new HighScoreData("cvb", 100, 5, 2));
        scores.add(new HighScoreData("cvb", 55, 13, 66));
        return scores;
    }*/

    public void setSrv(RMIMainServer srv) {
        this.srv = srv;
    }

    public void updateHighScoreTable() {
        //mainController.getHighScoreFromSQL();
        List list = null;
        ObservableList<HighScoreData> highScoreDataList = FXCollections.observableArrayList();
        try {
            list = srv.getQuery(
                    "from User u order by u.tttDiff desc",
                    5);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        assert list != null;
        for (Object u : list) {
            String userName = ((User) u).getUserName();
            int wins = ((User) u).getTttWins();
            int losses = ((User) u).getTttLoses();
            int diff = ((User) u).getTttDiff();
            highScoreDataList.add(
                    new HighScoreData(
                            userName + "(T)",
                            wins,
                            losses,
                            diff));
        }
        try {
            list = srv.getQuery(
                    "from User u order by u.chDiff desc",
                    5);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        assert list != null;
        for (Object u : list) {
            String userName = ((User) u).getUserName();
            int wins = ((User) u).getChWins();
            int losses = ((User) u).getTttLoses();
            int diff = ((User) u).getChDiff();
            highScoreDataList.add(
                    new HighScoreData(
                            userName + "(C)",
                            wins,
                            losses,
                            diff));
        }
        tableView.setItems(highScoreDataList);
        diffColumn.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().setAll(diffColumn);
    }
}