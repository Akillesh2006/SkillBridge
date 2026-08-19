package com.skillbridge.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    /**
     * Stores an uploaded file and returns the saved file path or reference key.
     */
    String storeFile(MultipartFile file, String fileName) throws IOException;

    /**
     * Retrieves the absolute local path or storage reference key for a stored file.
     */
    String getFilePath(String fileName);
}
