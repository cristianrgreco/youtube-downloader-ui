package com.cristianrgreco.ytdlui;

import com.cristianrgreco.ytdl.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
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
    private static final int CONCURRENT_REQUESTS = 5;
    private static final String DOWNLOAD_COMPLETE_STYLE = "-fx-accent: green;";
    private static final String DOWNLOAD_ERROR_STYLE = "-fx-accent: red;";

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

    public Controller() {
        this.currentDownloads = new HashSet<>();
        this.executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS, runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

    public void setDestinationDirectory() {
        this.destinationDirectoryService.prompt();
    }

    public void startDownload() {
        String urlString = this.urlTextfield.getText();

        if (urlString.trim().isEmpty()) {
            Platform.runLater(() -> this.alertFactory.alertForType(Alert.AlertType.WARNING, "Invalid URL", null, "Please enter a valid URL.").showAndWait());
            return;
        }

        if (!urlString.startsWith("http") && !urlString.startsWith("HTTP")) {
            urlString = "http://" + urlString;
        }

        URL videoUrl;
        try {
            videoUrl = new URL(urlString);
        } catch (MalformedURLException e) {
            Platform.runLater(() -> this.alertFactory.alertForType(Alert.AlertType.WARNING, "Invalid URL", null, "Please enter a valid URL.").showAndWait());
            return;
        }

        String videoId;
        try {
            Optional<NameValuePair> parameter = URLEncodedUtils.parse(videoUrl.toURI(), "UTF-8").stream()
                    .filter(param -> param.getName().equals("v"))
                    .findFirst();
            if (!parameter.isPresent()) {
                Platform.runLater(() -> this.alertFactory.alertForType(Alert.AlertType.WARNING, "Invalid URL", null, "Please enter a valid URL.").showAndWait());
                return;
            }
            videoId = parameter.get().getValue();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        if (this.currentDownloads.contains(videoId)) {
            Platform.runLater(() -> this.alertFactory.alertForType(Alert.AlertType.WARNING, "Download Error", null, "Download already in progress.").showAndWait());
            return;
        }

        this.urlTextfield.clear();

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

        Model model = new Model();
        model.setVideoId(videoId.toUpperCase());
        model.setUrlString(urlString);
        model.setOutputString(FilenameUtils.getBaseName(this.destinationDirectoryService.getDestinationDirectory().getPath()));
        model.setType(downloadType);
        model.setTypeImage(downloadTypeImageView);
        data.add(model);

        this.currentDownloads.add(videoId);

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

                StateChangeEvent stateChangeEvent = state -> Platform.runLater(() -> {
                    model.setStatus(state.toString());
                    if (state == State.COMPLETE) {
                        model.getProgressBar().setProgress(1);
                        model.getProgressBar().setStyle(DOWNLOAD_COMPLETE_STYLE);
                        this.currentDownloads.remove(videoId);
                    }
                    refreshDownloadsTable();
                });

                DownloadProgressUpdateEvent downloadProgressUpdateEvent = downloadProgress -> Platform.runLater(() -> {
                    model.getProgressBar().setProgress(downloadProgress.getPercentageComplete().doubleValue() / 100);
                    refreshDownloadsTable();
                    refreshDownloadsTable();
                });

                if (downloadType.equals("Audio")) {
                    ytdl.downloadAudio(Optional.of(stateChangeEvent), Optional.of(downloadProgressUpdateEvent));
                } else {
                    ytdl.downloadVideo(Optional.of(stateChangeEvent), Optional.of(downloadProgressUpdateEvent));
                }
            } catch (DownloadException e) {
                if (e.hasErrorOccurred()) {
                    Platform.runLater(() -> {
                        model.getProgressBar().setProgress(1);
                        model.getProgressBar().setStyle(DOWNLOAD_ERROR_STYLE);
                        this.alertFactory.errorForException(e, Optional.of("Download Error"), Optional.empty()).showAndWait();
                    });
                }
                e.printStackTrace();
            }
        });
    }

    void setBinaryConfiguration(BinaryConfiguration binaryConfiguration) {
        this.binaryConfiguration = binaryConfiguration;
    }

    void setAlertFactory(AlertFactory alertFactory) {
        this.alertFactory = alertFactory;
    }

    void setDestinationDirectoryService(DestinationDirectoryService destinationDirectoryService) {
        this.destinationDirectoryService = destinationDirectoryService;
    }

    private synchronized void refreshDownloadsTable() {
        ((TableColumn)this.downloadsTable.getColumns().get(0)).setVisible(false);
        ((TableColumn)this.downloadsTable.getColumns().get(0)).setVisible(true);
    }
}
