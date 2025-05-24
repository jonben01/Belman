package BLL.util;


import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PDFPreviewUtil {

    public static List<Image> convertPDFToFXImage(File pdf) throws Exception {
        //autocloseable
        List<Image> images = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdf)) {
            PDFRenderer renderer = new PDFRenderer(document);
            //split the PDF into its pages, so it displays better when it gets to the gui.
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                //renderImage creates a BufferedImage, which needs to be converted
                BufferedImage bufferedImage = renderer.renderImageWithDPI(i, 150);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                images.add(image);
            }
            System.out.println(images.size());
            return images;

        } catch (IOException e) {
            throw new Exception("issue converting PDF to image", e);
        }
    }
}
