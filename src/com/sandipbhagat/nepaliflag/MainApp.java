package com.sandipbhagat.nepaliflag;
/**
 * Created by sandip on 8/14/15.
 */

import com.sandipbhagat.nepaliflag.view.AppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
    }


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Apply Nepali Flag Colour On Your Profile Picture");

        try {
            // load root layout from fxml file
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/App.fxml"));
            BorderPane rootLayout = loader.load();
            rootLayout.setPrefSize(1024, 600);

            // show the scene containing the root layout
            Scene scene = new Scene(rootLayout);
            scene.getStylesheets().add(MainApp.class.getResource("view/style.css").toExternalForm());
            primaryStage.setScene(scene);
            AppController controller = loader.getController();
            controller.setMainApp(this);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

}
