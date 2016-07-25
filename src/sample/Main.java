package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.awt.*;
import java.awt.event.ActionEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        //First Screen
        primaryStage.setTitle("Thomas the Search Engine");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Thomas the Search Engine");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Image image = new Image(getClass().getResourceAsStream("train.png"));
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);
        grid.add(imageView, 0, 1);

        TextField userTextField = new TextField();
        userTextField.setPromptText("Search");
        GridPane.setConstraints(userTextField, 0, 2);
        grid.getChildren().add(userTextField);


        //Second Screen
        BorderPane borderPane = new BorderPane();

        Text text1 = new Text("new page");
        text1.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search");

        HBox hbox = new HBox();
        hbox.getChildren().addAll(text1, searchBar);
        borderPane.setTop(hbox);

        ObservableList<String> urlList = FXCollections.observableArrayList();
        ListView<String> viewUrlList = new ListView<String>(urlList);
        borderPane.setCenter(viewUrlList);

        //Buttons

        Button submit = new Button("Submit");
        GridPane.setConstraints(submit, 1,2);
        submit.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                primaryStage.setScene(new Scene(borderPane, 800, 500));
            }
        });
        grid.getChildren().add(submit);

        Scene scene = new Scene(grid, 800,500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
