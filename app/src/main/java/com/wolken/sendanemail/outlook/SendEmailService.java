package com.wolken.sendanemail.outlook;//package

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendEmailService {
    private static Context ctx;



    Properties prop;
    Session session;

    public SendEmailService(Context context,String username,String password) {
        ctx = context;
        
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);

        prop = new Properties();
        prop.put("mail.smtp.host", "smtp.office365.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.ssl.trust", "smtp.office365.com");

        session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
    }



    public void SendEmail(Bitmap bitmap,String fromAddress,String toAddress,String sub,String body) {
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toAddress)
            );
            message.setSubject(sub);
            //message.setText("Welcome to Medium!");

            Multipart multipart = new MimeMultipart();

            //text
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, "text/html");
            multipart.addBodyPart(messageBodyPart);

            //image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageInByte = baos.toByteArray();

            MimeBodyPart imageBodyPart = new MimeBodyPart();
            ByteArrayDataSource bds = new ByteArrayDataSource(imageInByte, "image/png");
            imageBodyPart.setDataHandler(new DataHandler(bds));
            imageBodyPart.setHeader("Content-ID", "<image>");

            imageBodyPart.setFileName("Example.png");
            multipart.addBodyPart(imageBodyPart);

            //attachment
            MimeBodyPart textBodyPart = new MimeBodyPart();
            ByteArrayDataSource tds = new ByteArrayDataSource("text".getBytes(Charset.forName("UTF-8")), "text/plain");
            textBodyPart.setDataHandler(new DataHandler(tds));
            textBodyPart.setHeader("Content-ID", "<text>");
            textBodyPart.setFileName("Example.txt");
            multipart.addBodyPart(textBodyPart);

            message.setContent(multipart);

            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
