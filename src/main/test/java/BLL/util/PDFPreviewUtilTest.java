package BLL.util;

import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PDFPreviewUtilTest {

    @Test
    void testConversion_shouldReturnImages() throws Exception {
        File pdf = new File("src/main/test/resources/test_pdf.pdf");

        assertTrue(pdf.exists());
        assertTrue(pdf.getName().endsWith(".pdf"), "pdf file not found");

        List<Image> images = PDFPreviewUtil.convertPDFToFXImage(pdf);

        assertNotNull(images);
        assertFalse(images.isEmpty());
    }

    @Test
    void testConversion_shouldThrowOnInvalidFile() throws Exception {
        File pdf = new File("src/main/test/resources/test_pdf.png");

        Exception exception = assertThrows(Exception.class, () -> PDFPreviewUtil.convertPDFToFXImage(pdf));

        assertFalse(pdf.getName().endsWith(".pdf"), "pdf file not found");
        assertTrue(exception.getMessage().contains("issue converting PDF to image"));
    }
}