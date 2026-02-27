package com.onetime.filelink;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@RestController
@RequestMapping("/api/files")
@CrossOrigin
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final Map<String, FileInfo> store = new HashMap<>();

    // ================= FILE UPLOAD =================
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("expiry") int expiry,
            HttpServletRequest request) throws IOException {

        String token = UUID.randomUUID().toString();

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String filePath = uploadDir + File.separator + token + "_" + file.getOriginalFilename();
        File savedFile = new File(filePath);
        file.transferTo(savedFile);

        long expiryTime = System.currentTimeMillis() + (expiry * 60 * 1000);

        FileInfo info = new FileInfo(
                filePath,
                file.getContentType(),
                expiryTime,
                null
        );

        store.put(token, info);

        // ===== Dynamic Link Generation =====
        String baseUrl = request.getRequestURL().toString()
                .replace(request.getRequestURI(), "");

        String fullLink = baseUrl + "/api/files/view/" + token;

        Map<String, String> response = new HashMap<>();
        response.put("link", fullLink);

        return ResponseEntity.ok(response);
    }

    // ================= MESSAGE UPLOAD =================
    @PostMapping("/uploadMessage")
    public ResponseEntity<Map<String, String>> uploadMessage(
            @RequestParam String message,
            @RequestParam int expiry,
            HttpServletRequest request) {

        String token = UUID.randomUUID().toString();

        long expiryTime = System.currentTimeMillis() + (expiry * 60 * 1000);

        FileInfo info = new FileInfo(
                null,
                null,
                expiryTime,
                message
        );

        store.put(token, info);

        // ===== Dynamic Link Generation =====
        String baseUrl = request.getRequestURL().toString()
                .replace(request.getRequestURI(), "");

        String fullLink = baseUrl + "/api/files/view/" + token;

        Map<String, String> response = new HashMap<>();
        response.put("link", fullLink);

        return ResponseEntity.ok(response);
    }

    // ================= VIEW PAGE =================
    @GetMapping("/view/{token}")
    public ResponseEntity<?> view(@PathVariable String token) {

        FileInfo info = store.get(token);

        if (info == null || System.currentTimeMillis() > info.getExpiryTime()) {
            store.remove(token);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Link expired or invalid");
        }

        // ===== MESSAGE VIEW =====
        if (info.getMessage() != null) {

            String safeMessage = info.getMessage()
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");

            store.remove(token); // ONE TIME USE

            String html =
                    "<html><body style='background:#111;color:white;text-align:center;padding-top:100px;font-family:sans-serif;'>" +
                            "<h2>🔐 Secure Message</h2>" +
                            "<div style='background:#222;padding:30px;border-radius:10px;display:inline-block;'>" +
                            "<p style='font-size:18px;'>" + safeMessage + "</p>" +
                            "<p style='color:red;'>This message has expired.</p>" +
                            "</div></body></html>";

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        }

        // ===== FILE VIEW WRAPPER =====
     String html =
        "<html><body style='background:#111;color:white;text-align:center;padding-top:100px;font-family:sans-serif;'>" +
                "<h2>🔐 Secure One-Time File</h2>" +
                "<a href='/api/files/download/" + token + "' style='background:#4CAF50;color:white;padding:15px 25px;text-decoration:none;border-radius:5px;font-size:18px;'>Open File</a>" +
                "<p style='color:red;margin-top:20px;'>File will expire after opening.</p>" +
                "</body></html>";

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    // ================= DOWNLOAD FILE =================
    @GetMapping("/download/{token}")
    public ResponseEntity<?> download(@PathVariable String token) {

        FileInfo info = store.get(token);

        if (info == null || System.currentTimeMillis() > info.getExpiryTime()) {
            store.remove(token);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("File expired");
        }

        File file = new File(info.getPath());
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        store.remove(token); // ONE TIME REMOVE

        MediaType mediaType = MediaType.parseMediaType(info.getContentType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getName() + "\"")
                .body(new FileSystemResource(file));
    }
}

