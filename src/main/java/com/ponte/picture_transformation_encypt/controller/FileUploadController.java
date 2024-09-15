package com.ponte.picture_transformation_encypt.controller;

import com.ponte.picture_transformation_encypt.modell.Picture;
import com.ponte.picture_transformation_encypt.service.PictureService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
public class FileUploadController {

    Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${upload.folder}")
    public String uploadFolder;

    @Value("${imageModifierProgramCommand}")
    public String command;

    @Value("${imageModifierProgramOption}")
    public String option;

    @Value("${keyPath}")
    public String keyPath;

    @Autowired
    private PictureService pictureService;

    @PostMapping("/api/files")
    @Operation(summary = "Uploads pictures", description = "Uploads picture/s with encryption into the DB.")
    public ResponseEntity<List<String>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("width") Integer width,
            @RequestParam("height") Integer height) {

        List<String> uploadedFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (!isFilePngOrJpg(fileName)) {
                failedFiles.add(fileName + " - Only PNG and JPG files are allowed.");
                continue;
            }

            try {
                if (isPicBiggerThanAllowed(file)) {
                    failedFiles.add(fileName + " - Image dimensions exceed the maximum allowed size (5000x5000 pixels).");
                    continue;
                }

                createTempFileAndResize(width, height, file, fileName);
                encryptPicture(fileName);
                storePictureInDatabaseThenDeleteIt(fileName);

                uploadedFiles.add(fileName);
            } catch (Exception e) {
                failedFiles.add(fileName + " - Failed to upload due to an internal error.");
                LOG.error("Error by filehandling :" + e);
            }
        }

        if (!failedFiles.isEmpty()) {
            List<String> response = new ArrayList<>();
            response.add("The following files were uploaded successfully: " + uploadedFiles);
            response.add("The following files failed to upload: " + failedFiles);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            return ResponseEntity.ok(uploadedFiles);
        }
    }

    public void createTempFileAndResize(Integer width, Integer height, MultipartFile file, String fileName) throws Exception {
        // Save the file temporarily
        Path tempPath = Paths.get(uploadFolder, "temp_" + fileName);
        Files.write(tempPath, file.getBytes());

        // Resize the image using 3rd party program
        ProcessBuilder pb = new ProcessBuilder(command, tempPath.toString(), option, width + "x" + height + "\\!", uploadFolder + "/" + fileName);
        Process p = pb.start();
        p.waitFor();

        // Delete the temporary file
        Files.delete(tempPath);
    }

    private void encryptPicture(String fileName) throws Exception {
        // Load the secret key
        SecretKey secretKey = loadSecretKey(keyPath+"secretKey.key");

        // Encrypt the resized picture
        Path resizedPath = Paths.get(uploadFolder, fileName);
        encryptPicture(resizedPath, secretKey);
    }

    private boolean isFilePngOrJpg(String fileName) {
        return fileName.toUpperCase().endsWith(".PNG") || fileName.toUpperCase().endsWith(".JPG");
    }

    private boolean isPicBiggerThanAllowed(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        int width = image.getWidth();
        int height = image.getHeight();
        return width > 5000 || height > 5000;
    }

    private SecretKey loadSecretKey(String keyPath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyPath))) {
            return (SecretKey) ois.readObject();
        }
    }

    private void encryptPicture(Path picturePath, SecretKey secretKey) throws Exception {
        // Read the picture data
        byte[] pictureData = Files.readAllBytes(picturePath);

        // Encrypt the picture data
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(pictureData);

        // Write the encrypted data back to the picture file
        Files.write(picturePath, encryptedData);
    }

    public void storePictureInDatabaseThenDeleteIt(String fileName) throws Exception {
        Path picturePath = Paths.get(uploadFolder, fileName);
        byte[] pictureData = Files.readAllBytes(picturePath);

        Picture picture = new Picture();
        picture.setName(fileName);
        picture.setData(pictureData);

        pictureService.savePicture(picture);

        Files.delete(picturePath);
    }

}


