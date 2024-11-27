package com.example;

import java.util.List;
import java.util.ArrayList;

import java.lang.ClassNotFoundException;
import java.lang.IndexOutOfBoundsException;

import java.net.Socket;
import java.net.UnknownHostException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.sql.*;

import javax.sql.rowset.CachedRowSet;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class Client extends Application{

    public static Client me; //Get the application instance in javafx
    public static Stage thePrimaryStage;  //Get the application primary scene in javafx
    private Socket clientSocket = null;

    private CachedRowSet serviceOutcome = null; //The service outcome


    //Convenient to populate the TableView
    public class MyTableRecord {
        private StringProperty recipe_name;
        private StringProperty prep_time;
        private StringProperty cook_time;
        private StringProperty total_time;
        private StringProperty difficulty;

        public void setRecipeName(String value) { recipeNameProperty().set(value); }
        public void setPrepTime(String value) { prepTimeProperty().set(value); }
        public void setCookTime(String value) { cookTimeProperty().set(value); }
        public void setTotalTime(String value) { totalTimeProperty().set(value); }
        public void setDifficulty(String value) { difficultyProperty().set(value); }



        public StringProperty recipeNameProperty() {
            if (recipe_name == null)
                recipe_name = new SimpleStringProperty(this, "");
            return recipe_name;
        }
        public StringProperty prepTimeProperty() {
            if (prep_time == null)
                prep_time = new SimpleStringProperty(this, "");
            return prep_time;
        }
        public StringProperty cookTimeProperty() {
            if (cook_time == null)
                cook_time = new SimpleStringProperty(this, "");
            return cook_time;
        }
        public StringProperty totalTimeProperty() {
            if (total_time == null)
                total_time = new SimpleStringProperty(this, "");
            return total_time;
        }
        public StringProperty difficultyProperty() {
            if (difficulty == null)
                difficulty = new SimpleStringProperty(this, "");
            return difficulty;
        }

    }



    public void initializeSocket(){

        //TO BE COMPLETED
        try {
            clientSocket = new Socket(Credentials.HOST, Credentials.PORT);
        }catch(UnknownHostException e){
            System.out.println("Client: Unknown host. " + e);
        }catch(IOException e){
            System.out.println("Client: I/O error. " + e);
        }

    }

    public void requestService() { //when the button is pressed call requestService
        System.out.println("Client: Button pressed. Requesting recipes\n");

        //TO BE COMPLETED

        //should happen when button is pressed
        Service serv = new Service(clientSocket); //idk if this is client socket or not lol
        serv.attendRequest();
    }

    public void reportServiceOutcome() {
        try {

            //TO BE COMPLETED

            InputStream outcomeStream = clientSocket.getInputStream();
            ObjectInputStream outcomeStreamReader = new ObjectInputStream(outcomeStream);
            serviceOutcome = (CachedRowSet) outcomeStreamReader.readObject();

            //TableView outputBox = (TableView) thePrimaryStage.getScene().getRoot(); //error is here

            //ObservableList<MyTableRecord> tmpRecords = outputBox.getItems();

            TableView<MyTableRecord> outputBox = new TableView<MyTableRecord>();
            GridPane grid = (GridPane) thePrimaryStage.getScene().getRoot();

            for(Node node : grid.getChildren()){
                if(node instanceof TableView){
                    outputBox = (TableView<MyTableRecord>) node;
                }
            }

            ObservableList<MyTableRecord> tmpRecords = outputBox.getItems();
            tmpRecords.clear();
            while (this.serviceOutcome.next()) {
                MyTableRecord record = new MyTableRecord();
                record.setRecipeName(serviceOutcome.getString("Recipe Name"));
                record.setPrepTime(serviceOutcome.getString("Prep Time"));
                record.setCookTime(serviceOutcome.getString("Cook Time"));
                record.setTotalTime(serviceOutcome.getString("Total Time"));
                record.setDifficulty(serviceOutcome.getString("Difficulty"));
                //System.out.println(record.getTitle() + " | " + record.getLabel() + record.getGenre() + " | " + record.getRrp() + " | " + record.getCopyID());

                tmpRecords.add(record);
            }
            outputBox.setItems(tmpRecords);


            String tmp = " ";
            System.out.println(tmp +"\n====================================\n");
        }catch(IOException e){
            System.out.println("Client: I/O error. " + e);
        }catch(ClassNotFoundException e){
            System.out.println("Client: Unable to cast read object to CachedRowSet. " + e);
        }catch(SQLException e){
            System.out.println("Client: Can't retrieve requested attribute from result set. " + e);
        }
    }

    public void execute(){
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
        }catch(Exception e){
            System.out.println(e);
        }

        try{
            //Initializes the socket
            this.initializeSocket();

            //Request service
            this.requestService();

            //Report user outcome of service
            this.reportServiceOutcome();

            //Close the connection with the server
            this.clientSocket.close();

        }catch(Exception e)
        {// Raised if connection is refused or other technical issue
            System.out.println("Client: Exception " + e);
        }
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Recipe Suggestions");

        //The main layout of the page
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        //This is the button you press to generate the recipes (fill out the table)
        //It is places in the top right of the grid pane
        Button generate = new Button();
        generate.setText("Generate Recipes");
        generate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                requestService();
            }
        });
        GridPane.setConstraints(generate, 0, 9, 2, 1);
        grid.getChildren().add(generate);

        //This is the filter button in the top left of the grid pane
        Button filter = new Button();
        filter.setText("Filter Recipes");
        filter.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //filterRecipes();
                //Make the filter recipes
            }
        });
        GridPane.setConstraints(filter, 0, 0, 2, 1);
        grid.getChildren().add(filter);

        //This is the output table where all the recipes will be listed
        TableView<RecipeTable> recipeTable = new TableView<MyTableRecord>();
        TableColumn<RecipeTable,String> recipe_name = new TableColumn<MyTableRecord,String>("Recipe Name");
        TableColumn<RecipeTable,String> prep_time = new TableColumn<MyTableRecord,String>("Prep Time");
        TableColumn<RecipeTable,String> cooking_time = new TableColumn<MyTableRecord,String>("Cook Time");
        TableColumn<RecipeTable,String> total_time = new TableColumn<MyTableRecord,String>("Total Time");
        TableColumn<RecipeTable,String> difficulty = new TableColumn<MyTableRecord,String>("Difficulty");

        recipe_name.setCellValueFactory(new PropertyValueFactory("Recipe Name"));
        prep_time.setCellValueFactory(new PropertyValueFactory("Prep Time"));
        cooking_time.setCellValueFactory(new PropertyValueFactory("Cooking Time"));
        total_time.setCellValueFactory(new PropertyValueFactory("Total Time"));
        difficulty.setCellValueFactory(new PropertyValueFactory("Difficulty"));

        GridPane.setConstraints(recipleTable, 0, 3, 3, 5);


    }

    public static void main (String[] args) {

    }

}
