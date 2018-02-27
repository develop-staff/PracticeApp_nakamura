package com.example.demo;

import org.springframework.web.multipart.MultipartFile;
public class UploadForm {
    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}