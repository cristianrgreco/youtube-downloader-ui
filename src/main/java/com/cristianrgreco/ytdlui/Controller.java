package com.cristianrgreco.ytdlui;

import com.cristianrgreco.ytdl.BinaryConfiguration;
import com.cristianrgreco.ytdl.DownloadException;
import com.cristianrgreco.ytdl.State;
import com.cristianrgreco.ytdl.YouTubeDownloaderAdapter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Controller {
    @FXML
    private GridPane root;
    @FXML
    private TextField urlTextfield;
    @FXML
    private ToggleGroup downloadType;
    @FXML
    private TableView downloadsTable;

    private Executor executor;
    private Set<String> currentDownloads;

    private BinaryConfiguration binaryConfiguration;
    private AlertFactory alertFactory;
    private DestinationDirectoryService destinationDirectoryService;

    void setBinaryConfiguration(BinaryConfiguration binaryConfiguration) {
        this.binaryConfiguration = binaryConfiguration;
    }

    void setAlertFactory(AlertFactory alertFactory) {
        this.alertFactory = alertFactory;
    }

    void setDestinationDirectoryService(DestinationDirectoryService destinationDirectoryService) {
        this.destinationDirectoryService = destinationDirectoryService;
    }

    public Controller() {
        this.currentDownloads = new HashSet<>();
        this.executor = Executors.newFixedThreadPool(5, runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

    public void setDestinationDirectory() {
        this.destinationDirectoryService.prompt();
//
//        Arrays.asList(
//                "https://www.youtube.com/watch?v=lWA2pjMjpBs",
//                "https://www.youtube.com/watch?v=YqeW9_5kURI",
//                "https://www.youtube.com/watch?v=jcF5HtGvX5I",
//                "https://www.youtube.com/watch?v=U0CGsw6h60k",
//                "https://www.youtube.com/watch?v=uelHwf8o7_U",
//                "https://www.youtube.com/watch?v=jV67jvvurCg",
//                "https://www.youtube.com/watch?v=KMOOr7GEkj8",
//                "https://www.youtube.com/watch?v=6JnGBs88sL0",
//                "https://www.youtube.com/watch?v=QrOe2h9RtWI",
//                "https://www.youtube.com/watch?v=Xcwd_Nz6Zog",
//                "https://www.youtube.com/watch?v=pa14VNsdSYM",
//                "https://www.youtube.com/watch?v=nhBorPm6JjQ",
//                "https://www.youtube.com/watch?v=rp4UwPZfRis&index=15&list=PL-8M5stZkYjoIoESZb5jRfOs1mWHS4LPH",
//                "https://www.youtube.com/watch?v=nhBorPm6JjQ&index=16&list=PL-8M5stZkYjoIoESZb5jRfOs1mWHS4LPH",
//                "https://www.youtube.com/watch?v=ZQ2nCGawrSY&list=PL-8M5stZkYjoIoESZb5jRfOs1mWHS4LPH&index=17",
//                "https://www.youtube.com/watch?v=KdS6HFQ_LUc&list=PL-8M5stZkYjoIoESZb5jRfOs1mWHS4LPH&index=19",
//                "https://www.youtube.com/watch?v=hD5MRBzY1uM&list=PL-8M5stZkYjoIoESZb5jRfOs1mWHS4LPH&index=20",
//                "https://www.youtube.com/watch?v=up7pvPqNkuU&index=21&list=PL-8M5stZkYjoIoESZb5jRfOs1mWHS4LPH"
//        ).stream().distinct().forEach(url -> {
//            this.urlTextfield.setText(url);
//            this.startDownload();
//        });
    }

    public void startDownload() {
        String urlString = this.urlTextfield.getText();

        if (urlString.trim().isEmpty()) {
            Platform.runLater(() -> this.alertFactory.alertForType(Alert.AlertType.WARNING, "Invalid URL", null, "Please enter a valid URL.").showAndWait());
            return;
        }

        if (this.currentDownloads.contains(urlString)) {
            Platform.runLater(() -> this.alertFactory.alertForType(Alert.AlertType.WARNING, "Download Error", null, "Download already in progress.").showAndWait());
            return;
        }

        this.urlTextfield.clear();

        final URL videoUrl;
        try {
            videoUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            Platform.runLater(() -> this.alertFactory.errorForException(e, Optional.of("Invalid URL"), Optional.of("The URL provided is invalid")).showAndWait());
            e.printStackTrace();
            return;
        }

        String videoId;
        try {
            videoId = URLEncodedUtils.parse(videoUrl.toURI(), "UTF-8").stream().filter(param -> param.getName().equals("v")).findFirst().get().getValue();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        String outputString = this.destinationDirectoryService.getDestinationDirectory().getPath();
        String downloadType = ((RadioButton) this.downloadType.getSelectedToggle()).getId().equals("audioType") ? "Audio" : "Video";

        Image downloadTypeImage;
        if (downloadType.equals("Audio")) {
            downloadTypeImage = new Image(getClass().getResource("/music.png").toString());
        } else {
            downloadTypeImage = new Image(getClass().getResource("/video.png").toString());
        }
        ImageView downloadTypeImageView = new ImageView();
        downloadTypeImageView.setFitHeight(12);
        downloadTypeImageView.setFitWidth(15);
        downloadTypeImageView.setImage(downloadTypeImage);

        ObservableList<Model> data = this.downloadsTable.getItems();

        final Model model = new Model();
        model.setVideoId(videoId.toUpperCase());
        model.setUrlString(urlString);
        model.setOutputString(FilenameUtils.getBaseName(outputString));
        model.setType(downloadType);
        model.setTypeImage(downloadTypeImageView);
        data.add(model);

        this.currentDownloads.add(urlString);
        this.executor.execute(() -> {
            YouTubeDownloaderAdapter ytdl = new YouTubeDownloaderAdapter(
                    videoUrl,
                    new File(this.destinationDirectoryService.getDestinationDirectory().getAbsolutePath()),
                    this.binaryConfiguration);

            try {
                String title = ytdl.getTitle();
                Platform.runLater(() -> {
                    model.setName(title);
                    refreshDownloadsTable();
                });
                if (downloadType.equals("Audio")) {
                    ytdl.downloadAudio(Optional.of(state -> Platform.runLater(() -> {
                        model.setStatus(state.toString());
                        if (state == State.CONVERTING) {
                            model.getProgressBar().setProgress(-1);
                        } else if (state == State.COMPLETE) {
//                                Media sound = new Media(getClass().getResource("/finished.mp3").toString());
//                                new MediaPlayer(sound).play();
                            model.getProgressBar().setProgress(1);
                            model.getProgressBar().setStyle("-fx-accent: green;");
                            this.currentDownloads.remove(urlString);
                        }
                        refreshDownloadsTable();
                    })), Optional.of(progress -> Platform.runLater(() -> {
                        model.getProgressBar().setProgress(progress.getPercentageComplete().doubleValue() / 100);
                        refreshDownloadsTable();
                        refreshDownloadsTable();
                    })));
                } else {
                    ytdl.downloadVideo(Optional.of(state -> Platform.runLater(() -> {
                        model.setStatus(state.toString());
                        if (state == State.CONVERTING) {
                            model.getProgressBar().setProgress(-1);
                        } else if (state == State.COMPLETE) {
//                                Media sound = new Media(getClass().getResource("/finished.mp3").toString());
//                                new MediaPlayer(sound).play();
                            model.getProgressBar().setProgress(1);
                            model.getProgressBar().setStyle("-fx-accent: green;");
                            this.currentDownloads.remove(urlString);
                        }
                        refreshDownloadsTable();
                    })), Optional.of(progress -> Platform.runLater(() -> {
                        model.getProgressBar().setProgress(progress.getPercentageComplete().doubleValue() / 100);
                        refreshDownloadsTable();
                    })));
                }
            } catch (DownloadException e) {
                Platform.runLater(() -> {
                    model.getProgressBar().setProgress(1);
                    model.getProgressBar().setStyle("-fx-accent: red;");
                    this.alertFactory.errorForException(e, Optional.of("Download Error"), Optional.empty()).showAndWait();
                });
                e.printStackTrace();
            }
        });
    }

    private synchronized void refreshDownloadsTable() {
        ((TableColumn)this.downloadsTable.getColumns().get(0)).setVisible(false);
        ((TableColumn)this.downloadsTable.getColumns().get(0)).setVisible(true);
    }
}
