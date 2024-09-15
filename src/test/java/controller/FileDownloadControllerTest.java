package controller;

import com.ponte.picture_transformation_encypt.controller.FileDownloadController;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileDownloadControllerTest {

    @Mock
    private PictureService pictureService;

    @InjectMocks
    private FileDownloadController fileDownloadController;

    String keyPath = "key/";

    @BeforeEach
    void init(){
        // Set up mock upload folder
        fileDownloadController.keyPath = keyPath;
    }

    @Test
    void testGetPictureByNameSuccess() throws Exception {
        // Set up mock data
        List<Picture> pictures = new ArrayList<>();
        Picture picture = new Picture();
        picture.setName("test_picture.png");
        picture.setData(new byte[0]);
        pictures.add(picture);

        when(pictureService.getPicturesByName(anyString())).thenReturn(pictures);

        // Call the method
        ResponseEntity<?> response = fileDownloadController.getPictureByName("test_picture.png");

        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetPictureByNameNotFound() throws Exception {
        // Set up mock data
        when(pictureService.getPicturesByName(anyString())).thenReturn(new ArrayList<>());

        // Call the method
        ResponseEntity<?> response = fileDownloadController.getPictureByName("test_picture.png");

        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDownloadAllFilesSuccess() throws Exception {
        // Set up mock data
        List<Picture> pictures = new ArrayList<>();
        Picture picture = new Picture();
        picture.setName("test_picture.png");
        picture.setData(new byte[0]);
        pictures.add(picture);

        when(pictureService.getAllPictures()).thenReturn(pictures);

        // Call the method
        ResponseEntity<?> response = fileDownloadController.downloadAllFiles();

        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDownloadAllFilesNotFound() throws Exception {
        // Set up mock data
        when(pictureService.getAllPictures()).thenReturn(new ArrayList<>());

        // Call the method
        ResponseEntity<?> response = fileDownloadController.downloadAllFiles();

        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}