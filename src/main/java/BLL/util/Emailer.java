package BLL.util;

import BE.Order;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Emailer {

    private static final String PROPERTY_FILE = "config/email.settings";

    public void sendEmail(String toEmail, File pdf, Order order) throws Exception {
        String orderNumber = order.getOrderNumber();

        //email config
        Properties emailConfig = new Properties();
        emailConfig.load(new FileInputStream(PROPERTY_FILE));

        String fromEmail = emailConfig.getProperty("email.username");
        String password = emailConfig.getProperty("email.password");
        String host = emailConfig.getProperty("email.smtp.host");
        String port = emailConfig.getProperty("email.smtp.port");

        //smtp config
        Properties smtpConfig = new Properties();
        smtpConfig.put("mail.smtp.auth", "true");
        smtpConfig.put("mail.smtp.starttls.enable", "true");
        smtpConfig.put("mail.smtp.port", port);
        smtpConfig.put("mail.smtp.host", host);

        //authentication (email + password)
        Session session = Session.getInstance(smtpConfig, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        //email structure + subject
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Belman Quality Assurance Report for order " + orderNumber);

        //mail body
        MimeBodyPart bodyPart = new MimeBodyPart();
    bodyPart.setText("Placeholder message telling you about your exciting Quality Assurance Report, " +
            "which can be found in the attachments!");

        //pdf attachment
        MimeBodyPart attachmentPart = new MimeBodyPart();
        DataSource source = new FileDataSource(pdf);
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName(orderNumber + "_Photo Documentation" + ".pdf");
        attachmentPart.setDisposition(MimeBodyPart.ATTACHMENT);

        //combine parts
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(bodyPart);
        multipart.addBodyPart(attachmentPart);

        //set content and send the mail
        message.setContent(multipart);
        Transport.send(message);

    }

}
