package com.cristianrgreco.ytdlui;

import java.io.File;

public interface BaseDestinationDirectoryService {
    void prompt();

    File getDestinationDirectory();
}
