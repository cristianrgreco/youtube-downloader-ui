package com.cristianrgreco.ytdlui;

import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.URL;

public class Model {
    private URL url;
    private File output;
    private String videoId;
    private String urlString;
    private String outputString;
    private String type;
    private ImageView typeImage;
    private String name;
    private String status;
    private ProgressBar progressBar;

    public Model() {
        this.progressBar = new ProgressBar();
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String id) {
        this.videoId = id;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getOutputString() {
        return outputString;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ImageView getTypeImage() {
        return typeImage;
    }

    public void setTypeImage(ImageView typeImage) {
        this.typeImage = typeImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
}
