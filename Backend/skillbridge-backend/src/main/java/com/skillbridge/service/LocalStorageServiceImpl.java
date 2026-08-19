package com.skillbridge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class LocalStorageServiceImpl implements FileStorageService {

    private final String uploadDir;

    public LocalStorageServiceImpl(
            @Value("${file.upload-dir:uploads}") String uploadDir
    ) {
        // Resolve path relative to working directory if not absolute
        File dir = new File(uploadDir);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), uploadDir);
        }
        this.uploadDir = dir.getAbsolutePath() + File.separator;
    }

    @Override
    public String storeFile(MultipartFile file, String fileName) throws IOException {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filePath = uploadDir + fileName;
        File destination = new File(filePath);
        file.transferTo(destination);
        return destination.getAbsolutePath();
    }

    @Override
    public String getFilePath(String fileName) {
        return uploadDir + fileName;
    }
}
