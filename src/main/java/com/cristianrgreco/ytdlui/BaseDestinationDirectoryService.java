package com.cristianrgreco.ytdlui;

import java.io.File;

interface BaseDestinationDirectoryService {
    void prompt();

    File getDestinationDirectory();
}
