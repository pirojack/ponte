package controller;

import com.ponte.picture_transformation_encypt.controller.FileUploadController;
import com.ponte.picture_transformation_encypt.modell.Picture;
import com.ponte.picture_transformation_encypt.service.PictureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileUploadControllerTest {

    @InjectMocks
    private FileUploadController fileUploadController;

    @Mock
    private PictureService pictureService;

    String uploadFolder = "src/test/resources";
    String ProgramCommand = "magick";
    String ProgramOption = "-resize";
    String keyPath = "key/";

    @BeforeEach
    void init(){
        // Set up mock upload folder
        fileUploadController.uploadFolder = uploadFolder;
        fileUploadController.command = ProgramCommand;
        fileUploadController.option = ProgramOption;
        fileUploadController.keyPath = keyPath;
    }

    @Test
    void testUploadFilesSuccess() throws IOException {

        // Create a mock multipart file with a test image
        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        byte[] imageBytes = bos.toByteArray();

        // Create mock multipart files
        MultipartFile file1 = new MockMultipartFile("files", "file1.png", "image/png", imageBytes);
        MultipartFile file2 = new MockMultipartFile("files", "file2.jpg", "image/jpeg", imageBytes);

        // Upload files
        ResponseEntity<List<String>> response = fileUploadController.uploadFiles(new MultipartFile[]{file1, file2}, 400, 300);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<String> uploadedFiles = response.getBody();
        assertNotNull(uploadedFiles);
        assertEquals(2, uploadedFiles.size());
        assertEquals("file1.png", uploadedFiles.get(0));
        assertEquals("file2.jpg", uploadedFiles.get(1));

    }

    @Test
    void testUploadFilesFailure() throws IOException {

        // Create a mock multipart file with a test image
        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        byte[] imageBytes = bos.toByteArray();

        // Create mock multipart files
        MultipartFile file1 = new MockMultipartFile("files", "file1.txt", "text/plain", imageBytes);
        MultipartFile file2 = new MockMultipartFile("files", "file2.jpg", "image/jpeg", imageBytes);

        // Upload files
        ResponseEntity<List<String>> response = fileUploadController.uploadFiles(new MultipartFile[]{file1, file2}, 400, 300);

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        List<String> responseList = response.getBody();
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("The following files were uploaded successfully: [file2.jpg]", responseList.get(0));
        assertEquals("The following files failed to upload: [file1.txt - Only PNG and JPG files are allowed.]", responseList.get(1));

    }

    @Test
    void testUploadFilesSizeExceedsLimit() throws IOException {
        // Create a mock multipart file with a test image that exceeds the maximum allowed size
        BufferedImage image = new BufferedImage(5001, 5001, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        byte[] imageBytes = bos.toByteArray();
        MultipartFile file = new MockMultipartFile("files", "test_image.png", "image/png", imageBytes);

        // Upload file
        ResponseEntity<List<String>> response = fileUploadController.uploadFiles(new MultipartFile[]{file}, 400, 300);

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        List<String> responseList = response.getBody();
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("The following files were uploaded successfully: []", responseList.get(0));
        assertEquals("The following files failed to upload: [test_image.png - Image dimensions exceed the maximum allowed size (5000x5000 pixels).]", responseList.get(1));
    }

    @Test
    void testCreateTempFileAndResizeSuccess() throws Exception {

        String originalFileName = "testpic.png";
        String newFileName = "copy_testpic.png";

        // Copy the test picture
        Path originalPath = Paths.get(uploadFolder, originalFileName);
        Path newPath = Paths.get(uploadFolder, newFileName);
        Files.copy(originalPath, newPath);

        // Create a mock file
        MultipartFile file = new MockMultipartFile("file", newFileName, "image/png", Files.readAllBytes(newPath));

        // Call the method
        fileUploadController.createTempFileAndResize(100, 100, file, newFileName);

        Path tempPath = Paths.get(uploadFolder, "temp_" + newFileName);

        assertFalse(Files.exists(tempPath));

        // Verify that the resized file is created
        Path resizedPath = Paths.get(uploadFolder, newFileName);
        assertTrue(Files.exists(resizedPath));

        // Check if the resized file has the correct dimensions
        BufferedImage image = ImageIO.read(resizedPath.toFile());
        int width = image.getWidth();
        int height = image.getHeight();
        assertEquals(100, width);
        assertEquals(100, height);

        // Clean up
        Files.delete(newPath);

    }

    @Test
    void testStorePictureInDatabaseThenDeleteItSuccess() throws Exception {

        String originalFileName = "testpic.png";
        String newFileName = "copy_testpic.png";

        // Copy the test picture
        Path originalPath = Paths.get(uploadFolder, originalFileName);
        Path newPath = Paths.get(uploadFolder, newFileName);
        Files.copy(originalPath, newPath);

        // Set up mock picture service
        doNothing().when(pictureService).savePicture(any(Picture.class));

        // Call the method
        fileUploadController.storePictureInDatabaseThenDeleteIt(newFileName);

        // Verify that the picture is saved in the database
        verify(pictureService).savePicture(any(Picture.class));

        // Verify that the file is deleted
        assert (!Files.exists(newPath));
    }

}