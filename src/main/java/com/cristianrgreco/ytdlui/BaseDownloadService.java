package com.cristianrgreco.ytdlui;

import com.cristianrgreco.ytdl.DownloadException;

import java.io.File;
import java.net.URL;

interface BaseDownloadService {
    void download(URL url, File destinationDirectory, Model model) throws DownloadException;

    boolean isDownloadInProgress(String url);
}
