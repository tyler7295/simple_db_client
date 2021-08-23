import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class DBEditorApplication extends Application {

    public static final String BLANK = "";
    private ObservableList<ObservableList> tableData;

    @Override
    public void start(Stage stage) {
        stage.setTitle("DB Editor");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        TextArea queryTextArea = new TextArea();
        grid.add(queryTextArea, 0, 0, 3, 1);

        Button goButton = new Button("Go");
        grid.add(goButton, 3, 0);

        TableView tableView = new TableView();
        grid.add(tableView, 0, 1, 4, 1);

        goButton.setOnAction(actionEvent -> {
            String query = queryTextArea.getText();
            if (BLANK.equals(query)) {
                this.alert("Input Missing", "Please enter the query!!", AlertType.ERROR);
            } else {
                Connection connection = null;
                PreparedStatement statement = null;
                tableData = FXCollections.observableArrayList();
                try {
                    connection = Database.getDBConnection();
                    connection.setAutoCommit(false);
                    statement = connection.prepareStatement(query);
                    ResultSet rs = statement.executeQuery();

                    tableView.getColumns().clear();
                    tableView.getItems().clear();

                    for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                        int j = i;
                        TableColumn column = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                        column.setCellValueFactory(
                                new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                            public ObservableValue<String> call(
                                    CellDataFeatures<ObservableList, String> param) {
                                return new SimpleStringProperty(param.getValue().get(j).toString());
                            }
                        });
                        tableView.getColumns().addAll(column);
                    }

                    while (rs.next()) {
                        ObservableList<String> row = FXCollections.observableArrayList();
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            row.add(rs.getString(i));
                        }
                        tableData.add(row);
                    }
                    tableView.setItems(tableData);
                } catch (SQLException se) {
                    this.alert("Error", "Invalid SQL or some other DB Error", AlertType.ERROR);
                    se.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != statement) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    if (null != connection) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });

        Scene scene = new Scene(grid, 500, 275);
        stage.setScene(scene);

        stage.show();
    }

    public void alert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
