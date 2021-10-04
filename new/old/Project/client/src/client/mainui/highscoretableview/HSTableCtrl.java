package client.mainui.highscoretableview;

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


// tableview extention for high score display on main window.
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
    // init columns with property factories.
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<HighScoreData, String>("user"));
        diffColumn.setCellValueFactory(new PropertyValueFactory<HighScoreData, Integer>("diff"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<HighScoreData, Integer>("wins"));
        lossesColumn.setCellValueFactory(new PropertyValueFactory<HighScoreData, Integer>("losses"));
        tableView.setEditable(false);
    }

    // main server setter
    public void setSrv(RMIMainServer srv) {
        this.srv = srv;
    }


    // update table with db data.
    public void updateHighScoreTable() {
        List list = null;
        ObservableList<HighScoreData> highScoreDataList
                = FXCollections.observableArrayList();

        // query and extract ttt data
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

        // query and extract ttt red data
        try {
            list = srv.getQuery(
                    "from User u order by u.tttDiffRed desc",
                    5);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        assert list != null;
        for (Object u : list) {
            String userName = ((User) u).getUserName();
            int wins = ((User) u).getTttWinsRed();
            int losses = ((User) u).getTttLossesRed();
            int diff = ((User) u).getTttDiffRed();
            highScoreDataList.add(
                    new HighScoreData(
                            userName + "(R)",
                            wins,
                            losses,
                            diff));
        }
        // update tableview with extracted data
        tableView.setItems(highScoreDataList);
        // sort by diff column
        diffColumn.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().setAll(diffColumn);
    }
}