package com.ponte.picture_transformation_encypt.controller;

import com.ponte.picture_transformation_encypt.modell.Picture;
import com.ponte.picture_transformation_encypt.service.PictureService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class FileDownloadController {

    Logger LOG = LoggerFactory.getLogger(FileDownloadController.class);

    @Autowired
    private PictureService pictureService;

    @Value("${keyPath}")
    public String keyPath;

    @GetMapping("/api/file/{fileName}")
    @Operation(summary = "Downloads picture", description = "Downloads the first picture what is find with the name in the DB.")
    public ResponseEntity<?> getPictureByName(@PathVariable String fileName) throws Exception {

        List<Picture> pictures = pictureService.getPicturesByName(fileName);

        if (pictures.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getFileNotFoundError());
        }

        SecretKey secretKey = loadSecretKey(keyPath + "secretKey.key");
        Picture picture = pictures.get(0);
        byte[] decryptedData = decryptPicture(picture.getData(), secretKey);
        String mimeType = getMimeType(picture.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(mimeType));
        headers.setContentDispositionFormData("attachment", picture.getName());

        return ResponseEntity.ok().headers(headers).body(decryptedData);
    }

    @GetMapping("/api/allFiles")
    @Operation(summary = "Downloads ZIP archive", description = "Downloads all pictures what is in the database table PICTURES.")
    public ResponseEntity<?> downloadAllFiles() throws Exception {

        List<Picture> pictures = pictureService.getAllPictures();

        if (pictures.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getFileNotFoundError());
        }

        SecretKey secretKey = loadSecretKey(keyPath + "secretKey.key");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        Map<String, Integer> filenameCounters = new HashMap<>();

        for (Picture picture : pictures) {
            byte[] decryptedData = decryptPicture(picture.getData(), secretKey);
            String filename = picture.getName();
            int counter = filenameCounters.getOrDefault(filename, 0);
            if (counter > 0) {
                int extensionIndex = filename.lastIndexOf('.');
                if (extensionIndex != -1) {
                    filename = filename.substring(0, extensionIndex) + "_" + counter + filename.substring(extensionIndex);
                } else {
                    filename = filename + "_" + counter;
                }
            }
            filenameCounters.put(picture.getName(), counter + 1);

            ZipEntry entry = new ZipEntry(filename);
            entry.setSize(decryptedData.length);
            zos.putNextEntry(entry);
            zos.write(decryptedData);
            zos.closeEntry();
        }

        zos.close();

        byte[] zipData = bos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "allPictures.zip");

        return ResponseEntity.ok().headers(headers).body(zipData);
    }

    private String getMimeType(String fileName) {
        if (fileName.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "image/unknown";
        }
    }

    private SecretKey loadSecretKey(String keyPath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyPath))) {
            return (SecretKey) ois.readObject();
        }
    }

    // Method to decrypt the picture
    private byte[] decryptPicture(byte[] encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }

    private String getFileNotFoundError() {
        return """
                <html>
                <head>
                    <title>File Not Found</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            text-align: center;
                            margin-top: 20%;
                        }
                    </style>
                </head>
                <body>
                    <h1>File not found</h1>
                </body>
                </html>""";
    }

}
