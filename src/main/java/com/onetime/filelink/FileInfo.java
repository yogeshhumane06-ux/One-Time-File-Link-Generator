package com.onetime.filelink;

public class FileInfo {

    private String path;
    private String contentType;
    private long expiryTime;
    private String message;

    public FileInfo(String path, String contentType, long expiryTime, String message) {
        this.path = path;
        this.contentType = contentType;
        this.expiryTime = expiryTime;
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public String getContentType() {
        return contentType;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public String getMessage() {
        return message;
    }
}
