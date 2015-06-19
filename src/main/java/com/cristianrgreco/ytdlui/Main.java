package com.cristianrgreco.ytdlui;

import com.cristianrgreco.ytdl.BinaryConfiguration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view.fxml"));

        primaryStage.getIcons().add(new Image(getClass().getResource("/icon.png").toString()));
        primaryStage.setTitle("YouTube Downloader");
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.show();

        Controller controller = loader.<Controller>getController();
        controller.setBinaryConfiguration(new BinaryConfiguration(
                new File("/Users/cristiangreco/Desktop/youtube-dl"),
                new File("/Users/cristiangreco/Desktop/ffmpeg")));
        controller.setAlertFactory(new AlertFactory(Optional.of(Arrays.asList("/view.css"))));
        controller.setDestinationDirectoryService(new DestinationDirectoryService(primaryStage.getScene().getWindow()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
