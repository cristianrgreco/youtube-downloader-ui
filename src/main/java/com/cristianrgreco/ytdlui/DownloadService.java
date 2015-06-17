package com.cristianrgreco.ytdlui;

import com.cristianrgreco.ytdl.BinaryConfiguration;
import com.cristianrgreco.ytdl.DownloadException;
import com.cristianrgreco.ytdl.YouTubeDownloaderAdapter;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DownloadService implements BaseDownloadService {
    private BinaryConfiguration binaryConfiguration;
    private Executor executor;
    private Set<String> currentDownloads;

    public DownloadService(BinaryConfiguration binaryConfiguration) {
        this.binaryConfiguration = binaryConfiguration;
        this.executor = Executors.newFixedThreadPool(5, runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        this.currentDownloads = new HashSet<>();
    }

    @Override
    public void download(URL url, File destinationDirectory, Model model) throws DownloadException {
//        this.executor.execute(() -> {
//            YouTubeDownloaderAdapter ytdl = new YouTubeDownloaderAdapter(url, destinationDirectory, this.binaryConfiguration);
//            String title = ytdl.getTitle();
//        });
    }

    @Override
    public boolean isDownloadInProgress(String url) {
        return this.currentDownloads.contains(url);
    }
}
