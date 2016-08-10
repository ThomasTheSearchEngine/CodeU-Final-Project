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
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.control.Hyperlink;
import java.util.LinkedList;
import java.util.Arrays;
import javafx.scene.layout.HBox;
import redis.clients.jedis.Jedis;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        //Set Up

        Jedis jedis = JedisMaker.make();
        JedisIndex index = new JedisIndex(jedis);
        LinkedList<VBox> realUrls = new LinkedList<VBox>();
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();


        //First Screen
        primaryStage.setTitle("Thomas the Search Engine");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Image image = new Image(getClass().getResourceAsStream("train.png"));
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);
        grid.add(imageView, 0, 0);

        TextField userTextField = new TextField();
        userTextField.setPromptText("Search Here");
        GridPane.setConstraints(userTextField, 0, 1);
        grid.getChildren().add(userTextField);

        ObservableList<VBox> urlList = FXCollections.observableArrayList();
        ListView<VBox> viewUrlList = new ListView<VBox>(urlList);



        //Second Screen
        BorderPane borderPane = new BorderPane();
        Scene scene2 = new Scene(borderPane, 800, 800);

        Image image1 = new Image(getClass().getResourceAsStream("train.png"));
        ImageView imageView1 = new ImageView();
        imageView1.setImage(image);
        imageView1.setFitWidth(100);
        imageView1.setPreserveRatio(true);

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search");

        HBox hbox = new HBox();
        hbox.getChildren().addAll(imageView1, searchBar);
        borderPane.setTop(hbox);

        borderPane.setCenter(viewUrlList);

        //Third Screen
        VBox browserScreen = new VBox();
        Scene scene3 = new Scene(browserScreen);
        HBox backButton = new HBox();



        //Buttons

        Button submit = new Button("Submit");
        GridPane.setConstraints(submit, 1,1);
        submit.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                primaryStage.setScene(scene2);
                String term = userTextField.getText();
                WikiSearch search = WikiSearch.search(term, index);
                LinkedList<Entry<String,Integer>> sorted = (LinkedList) search.sort();
                LinkedList<String> urls = new LinkedList<String>();
                for(Entry entry:sorted) {
                    urls.add(0,(String)entry.getKey());
                }
                realUrls.clear();
                for(String url:urls) {
                    VBox vbox = new VBox();
                    Text label = new Text(url);
                    String realUrl = url.substring(30).replace("_"," ");
                    Hyperlink link = new Hyperlink(realUrl);
                    vbox.getChildren().add(link);
                    vbox.getChildren().add(label);
                    link.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent event) {
                            webEngine.load(url);
                            backButton.getChildren().clear();
                            backButton.getChildren().addAll(imageView1,userTextField, submit);
                            browserScreen.getChildren().clear();
                            browserScreen.getChildren().addAll(backButton, browser);
                            primaryStage.setScene(scene3);
                        }
                    });
                    realUrls.add(vbox);
                }


                urlList.clear();
                urlList.addAll(realUrls);
            }
        });
        grid.getChildren().add(submit);

        Button submit2 = new Button("Submit");
        hbox.getChildren().add(submit2);
        submit2.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                String term = searchBar.getText();
                WikiSearch search = WikiSearch.search(term, index);
                LinkedList<Entry<String,Integer>> sorted = (LinkedList) search.sort();
                LinkedList<String> urls = new LinkedList<String>();
                for(Entry entry:sorted) {
                    urls.add(0,(String)entry.getKey());
                }
                realUrls.clear();
                for(String url:urls) {
                    VBox vbox = new VBox();
                    Text label = new Text(url);
                    String realUrl = url.substring(30).replace("_"," ");
                    Hyperlink link = new Hyperlink(realUrl);
                    vbox.getChildren().add(link);
                    vbox.getChildren().add(label);
                    link.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent event) {
                            webEngine.load(url);
                            backButton.getChildren().clear();
                            backButton.getChildren().addAll(imageView1, searchBar,submit);
                            browserScreen.getChildren().clear();
                            browserScreen.getChildren().addAll(backButton, browser);
                            primaryStage.setScene(scene3);
                        }
                    });
                    realUrls.add(vbox);
                }
                urlList.clear();
                urlList.addAll(realUrls);
            }
        });
        borderPane.setTop(hbox);

        Scene scene = new Scene(grid, 800,800);
        scene.getStylesheets().add
                (Main.class.getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
