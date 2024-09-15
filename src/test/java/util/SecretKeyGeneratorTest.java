package util;

import com.ponte.picture_transformation_encypt.util.SecretKeyGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SecretKeyGeneratorTest {

    @Test
    void testGenerateSecretKeySuccess() throws Exception {
        // Set up test data
        String keyPath = "src/test/resources/";

        // Call the method
        SecretKeyGenerator.generateSecretKey(keyPath);

        // Verify the result
        File file = new File(keyPath + "secretKey.key");
        assertTrue(file.exists());
        file.delete();
    }
}
