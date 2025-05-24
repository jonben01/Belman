package BLL.util;

import BE.Order;
import BE.Photo;
import BE.Product;
import GUI.util.SessionManager;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;


import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ReportGenerator implements IReportGenerator {

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public File generatePreview(Order order, String comment) throws Exception {
        File temp = File.createTempFile("temp_qc_preview_" + order.getOrderNumber(), ".pdf");
        if (comment == null || comment.trim().isEmpty()) {
            comment = "COMMENT GOES HERE";
        }
        generateReport(order, temp.getAbsolutePath(), comment);
        temp.deleteOnExit();
        return temp;
    }

    @Override
    public void generateReport(Order order, String filePath, String comment) throws Exception {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        document.setMargins(16, 30, 16, 30);

        //Header
        Table header = new Table(UnitValue.createPercentArray(new float[]{2,3}))
                .useAllAvailableWidth().setMarginBottom(0);
        //Logo (header)
        Image logo = new Image(ImageDataFactory.create(
                Objects.requireNonNull(getClass().getClassLoader().getResource("images/BELMAN_Logo_264pxl.png"))))
                .scaleToFit(100, 100);
        Cell logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER);
        header.addCell(logoCell);


        //Information/Metadata (header)
        //this should really be the order date, but since order numbers aren't really a part of this application,
        //I will just now.
        LocalDateTime now = LocalDateTime.now();
        String date = dateTimeFormatter.format(now);
        String orderNumber = order.getOrderNumber();
        String sentBy = SessionManager.getInstance().getCurrentUser()
                .getFirstName() + " " + SessionManager.getInstance().getCurrentUser().getLastName();
        Paragraph info = new Paragraph().add("Date: " + date + "\nOrder number: " + orderNumber + "\nApproved by: " + sentBy)
                .setTextAlignment(TextAlignment.RIGHT).setFontSize(10);
        Cell infoCell = new Cell().add(info).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        header.addCell(infoCell);

        document.add(header);

        //divider
        Paragraph divider = new Paragraph().add(" ")
                .setBackgroundColor(ColorConstants.BLUE).setHeight(5).setMarginBottom(10);
        document.add(divider);

        //title (says Quality Assurance Report Document)
        Paragraph title = new Paragraph().add("Quality Assurance Report Document")
                .setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER).setMarginBottom(10);
        document.add(title);

        //intro text
        Paragraph introText = new Paragraph().add("This report contains photo " +
                "documentation for all products in the order " + orderNumber + ".").setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT).setMarginBottom(10);
        document.add(introText);

        //user comment
        //TODO this will look ugly, fix it when I have time
        if (comment != null && !comment.trim().isEmpty()) {
            //If called through the preview method, center the comment text and make it red.
            if (comment.equals("COMMENT GOES HERE")) {
                Paragraph userComment = new Paragraph().add("COMMENT GOES HERE").setFontSize(12)
                        .setFontColor(ColorConstants.RED).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10);
                document.add(userComment);
            } else {
                Paragraph userComment = new Paragraph().add("Additional notes:\n " + comment)
                        .setFontSize(12).setTextAlignment(TextAlignment.LEFT).setMarginBottom(10);
                document.add(userComment);
            }
        }

        float pageWidth = PageSize.A4.getWidth() - document.getLeftMargin() - document.getRightMargin();
        float pageHeight = PageSize.A4.getHeight() - document.getTopMargin() - document.getBottomMargin();

        //Products and Photos
        for (Product p : order.getProducts()) {
            //document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            Paragraph productLabel = new Paragraph("Product: " + p.getProductNumber())
                    .setFontSize(13).setBold().setFontColor(ColorConstants.BLUE)
                    .setMarginTop(10).setMarginBottom(5).setTextAlignment(TextAlignment.LEFT);
            document.add(productLabel);

            for (Photo photo : p.getPhotos()) {
                String imagePath = photo.getFilePath();
                if (new File(imagePath).exists()) {
                    Image image = new Image(ImageDataFactory.create(imagePath));
                    image.scaleToFit(pageWidth, pageHeight * 0.4f);
                    image.setMarginBottom(10);
                    image.setHorizontalAlignment(HorizontalAlignment.LEFT);
                    document.add(image);
                }
            }
        }
        document.close();
    }
}
