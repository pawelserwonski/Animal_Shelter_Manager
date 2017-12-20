package ski.serwon.AnimalShelterManager.model;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource(File.separator + "ski" +
                File.separator + "serwon" + File.separator + "AnimalShelterManager" + File.separator
                + "view" + File.separator + "MainWindow.fxml"));

        primaryStage.setTitle("Animal Shelter Manager");
        primaryStage.setScene(new Scene(root, 800, 450));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
