package com.cristianrgreco.ytdlui;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public class DestinationDirectoryService implements BaseDestinationDirectoryService {
    private Window window;
    private File destinationDirectory;

    DestinationDirectoryService(Window window) {
        this.window = window;
        this.destinationDirectory = new File(System.getProperty("user.home"));
    }

    @Override
    public void prompt() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(this.destinationDirectory);
        File newDestination = directoryChooser.showDialog(this.window);
        if (newDestination != null) {
            this.destinationDirectory = newDestination;
        }
    }

    @Override
    public File getDestinationDirectory() {
        return this.destinationDirectory;
    }
}
