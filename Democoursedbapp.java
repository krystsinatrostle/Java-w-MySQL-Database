/*
 * Allows a user to add patients in the GUI that are automatically stored in the
 * MySQL databse
 */
package democoursedbapp;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.geometry.Insets;

/**
 *
 * @author Krystsina.trostle
 */

public class Democoursedbapp extends Application {

    //Declare connection
    //NOTE - Requires the MySQL JDBC driver has been added to Libraries
    private Connection conn;

    //Declare textfields for form
    private TextField tfPatientID = new TextField();
    private TextField tfPFirstName = new TextField();
    private TextField tfPLastName = new TextField();
    private TextField tfAge = new TextField();
    private TextField tfGender = new TextField();
    private TextField tfInsuranceNumber = new TextField();

    //Declare textArea for display
    private TextArea taShowRecords = new TextArea();

    //Declare labels
    private Label lbPatientID = new Label("Patient ID:");
    private Label lbPFirstName = new Label("Patient First Name:");
    private Label lbPLastName = new Label("Patient Last Name:");
    private Label lbAge = new Label("Age:");
    private Label lbGender = new Label("Gender:");
    private Label lbInsuranceNumber = new Label("Insurance Number:");
    private Label lbStatus = new Label("Status:");

    //Declare buttons
    private Button btInsert = new Button("Insert");
    private Button btUpdate = new Button("Update");
    private Button btDelete = new Button("Delete");
    private Button btSearch = new Button("Search");
    private Button btCLS = new Button("CLS");

    //Declare JavaFX HBox to organize buttons
    private HBox hbButtons = new HBox();

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {

        //Call method defined below that creates connection to DB
        initializeDB();

        //Create UI
        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setPadding(new Insets(20));
        gridPane.add(lbStatus, 0, 0);
        gridPane.add(lbPatientID, 0, 1, 1, 1);
        gridPane.add(tfPatientID, 1, 1, 1, 1);
        gridPane.add(lbPFirstName, 0, 2, 1, 1);
        gridPane.add(tfPFirstName, 1, 2, 1, 1);
        gridPane.add(lbPLastName, 0, 3, 1, 1);
        gridPane.add(tfPLastName, 1, 3, 1, 1);
        gridPane.add(lbAge, 0, 4, 1, 1);
        gridPane.add(tfAge, 1, 4, 1, 1);
        gridPane.add(lbGender, 0, 5, 1, 1);
        gridPane.add(tfGender, 1, 5, 1, 1);
        gridPane.add(lbInsuranceNumber, 0, 6, 1, 1);
        gridPane.add(tfInsuranceNumber, 1, 6, 1, 1);
        hbButtons.getChildren().addAll(btSearch, btInsert, btUpdate, btDelete, btCLS);
        hbButtons.setAlignment(Pos.CENTER);
        gridPane.add(hbButtons, 0, 7, 2, 1);
        gridPane.add(taShowRecords, 0, 8, 2, 4);
        gridPane.setAlignment(Pos.TOP_LEFT);

        // Create event handlers for buttons
        btInsert.setOnAction(e -> insertRecord());
        btCLS.setOnAction(e -> clearFields());
        btSearch.setOnAction(e -> searchRecord());
        btUpdate.setOnAction(e -> updateRecord());
        btDelete.setOnAction(e -> deleteRecord());

        // Create a scene and place it in the stage
        Scene scene = new Scene(gridPane, 400, 650);
        primaryStage.setTitle("Patients DB Demonstration App"); // Set title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        showRecords();  //showRecords called on init and any method that changes DB    
    }

    //This method is for the delete button to delete active record.
    private void deleteRecord() {
        String queryString = "delete from patients where patientID = ?;";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(queryString);
            preparedStatement.setString(1, tfPatientID.getText());
            preparedStatement.executeUpdate();
            showAlert("Delete worked!");
            //After db change, update list at bottom
            showRecords();
        } catch (SQLException e2) {
            e2.printStackTrace();
            showAlert("Delete Failed");
            //clearFields(); //optional clear form on delete fail
        }
    }

    //This method is for the insert button to insert values in form.
    private void insertRecord() {
        String queryString = "insert into patients (patientId, pFirstName, pLastName, age, gender, insuranceNumber) values (?,?,?,?,?,?);";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(queryString);
            preparedStatement.setString(1, tfPatientID.getText());
            preparedStatement.setString(2, tfPFirstName.getText());
            preparedStatement.setString(3, tfPLastName.getText());
            preparedStatement.setInt(4, Integer.parseInt(tfAge.getText()));
            preparedStatement.setString(5, tfGender.getText());
            preparedStatement.setInt(6, Integer.parseInt(tfInsuranceNumber.getText()));
            preparedStatement.executeUpdate();
            showAlert("Insert worked!");
            //After db change, update list at bottom
            showRecords();
        } catch (SQLException e2) {
            e2.printStackTrace();
            showAlert("Insert Failed");
            //clearFields(); //optional clear form on delete fail
        }
    }

    private void updateRecord() {
        String queryString = "update patients set patientId = ?, pFirstName = ?, pLastName = ?, age = ?, gender = ?, insuranceNumber = ? where patientID = ?";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(queryString);
            preparedStatement.setInt(1, Integer.parseInt(tfPatientID.getText()));
            preparedStatement.setString(2, tfPFirstName.getText());
            preparedStatement.setString(3, tfPLastName.getText());
            preparedStatement.setInt(4, Integer.parseInt(tfAge.getText()));
            preparedStatement.setString(5, tfGender.getText());
            preparedStatement.setInt(6, Integer.parseInt(tfInsuranceNumber.getText()));
            preparedStatement.setInt(7, Integer.parseInt(tfPatientID.getText()));
            preparedStatement.executeUpdate();
            showAlert("Update worked!");
            //After db change, update list at bottom
            showRecords();
        } catch (SQLException e2) {
            e2.printStackTrace();
            showAlert("Update Failed");
            //clearFields(); //optional clear form on update fail
        }
    }

    private void searchRecord() {
        //type a patientID and search
        String queryString = "Select * from patients where patientId = ?;";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(queryString);
            preparedStatement.setString(1, tfPatientID.getText());
            ResultSet rset = preparedStatement.executeQuery();
            if (rset.next()) {
                tfPatientID.setText(rset.getString("patientID"));
                tfPFirstName.setText(rset.getString("pFirstName"));
                tfPLastName.setText(rset.getString("pLastName"));
                tfAge.setText(String.valueOf(rset.getInt("age")));
                tfGender.setText(rset.getString("gender"));
                tfInsuranceNumber.setText(rset.getString("insuranceNumber"));
            } else {
                showAlert("No Record Found with ID of " + tfPatientID.getText());
                clearFields();
            }
        } catch (SQLException e2) {
            e2.printStackTrace();
            showAlert("Search Failed");
        }
    }

    private void showRecords() {
        //Utility method - update list at bottom of form whenever any update
        //is called, and on program load
        String queryString = "Select * from patients";
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(queryString);
            ResultSet rset2 = preparedStatement.executeQuery();
            String output = new String("");
            //Counter here - loop through the retrieved records.
            //If we finish the loop without changing count,
            //DB table was empty
            int count = 0;
            taShowRecords.setText("");
            while (rset2.next()) {
                count += 1;
                output = output + rset2.getString("patientID") + " " + rset2.getString("pFirstName") + " " + rset2.getString("pLastName") + "\n";
            }
            taShowRecords.setText(output);
            //If count remained 0, no records, so display an error.
            if (count == 0) {
                taShowRecords.setText("No records found.");
            }
        } catch (SQLException e2) {
            e2.printStackTrace();
            showAlert("Insert Failed");
            //clearFields();
        }
    }

    private void clearFields() {
        //Utility method to clear the textFields from a few different spots
        tfPatientID.setText("");
        tfPFirstName.setText("");
        tfPLastName.setText("");
        tfAge.setText("");
        tfGender.setText("");
        tfInsuranceNumber.setText("");
    }

    private void showAlert(String message) {
        //Utility method to trigger alert box
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(message);
        alert.setHeaderText(message);
        alert.setContentText(message);
        alert.showAndWait();

    }

    //Build the connection to the DB.
    private void initializeDB() {
        try {
            // Creates a new instance of the class and hence causes the Driver class to be initialized and in turn 
            // the class registers itself w/DriverManager to create mysql conn based on the url
            // Note: application no longer neeeds to explictly load JDBC drivers using Class.forName(),included
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/projectdb", "projectuser", "projectuser");
        } catch (Exception ex) {
            // Pinpoints the exact line, prints throwable along with line number and class name 
            ex.printStackTrace();
            showAlert("Connection failed - check DB is created");
        }
    }

    //Starts the JavaFX main form
    public static void main(String[] args) {
        Application.launch(args);
    }
}
