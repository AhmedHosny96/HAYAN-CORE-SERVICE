package com.hayaan.config;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.hayaan.flight.object.dto.BoardingDto;
import com.hayaan.flight.object.dto.flight.AirInfoResponse;
import com.hayaan.flight.object.dto.flight.PriceInfoResponse;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Slf4j
@Service
public class UtilService {


    @Value("${template.path}")
    private String templatePath;  // Inject the externalized path

    // TODO: GENERATE BOARDING PASS
    public ByteArrayOutputStream generatePdfWithCustomBarcode(BoardingDto boardingPassDTO) throws Exception {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            // Load the PDF template from the externalized path
            File pdfTemplateFile = new File(templatePath);

            // Check if the file exists
            if (!pdfTemplateFile.exists()) {
                throw new FileNotFoundException("Template PDF not found at " + templatePath);
            }

            InputStream templateStream = new FileInputStream(pdfTemplateFile);

            // Initialize PdfReader using the InputStream
            PdfReader pdfReader = new PdfReader(templateStream);
            PdfWriter pdfWriter = new PdfWriter(byteArrayOutputStream); // Write PDF to ByteArrayOutputStream
            PdfDocument pdfDoc = new PdfDocument(pdfReader, pdfWriter);

            // Initialize the document object
            Document document = new Document(pdfDoc);

            // Adding dynamic text data aligned with the PDF template layout
            document.showTextAligned(new Paragraph("Name: " + boardingPassDTO.getName()), 155, 670, TextAlignment.LEFT);
            document.showTextAligned(new Paragraph("Flight Number: " + boardingPassDTO.getFlight()), 155, 650, TextAlignment.LEFT);
            document.showTextAligned(new Paragraph("Seat: " + boardingPassDTO.getSeat()), 155, 630, TextAlignment.LEFT);
            document.showTextAligned(new Paragraph("From: " + boardingPassDTO.getFrom()), 155, 610, TextAlignment.LEFT);
            document.showTextAligned(new Paragraph("To: " + boardingPassDTO.getTo()), 155, 590, TextAlignment.LEFT);
            document.showTextAligned(new Paragraph("Departure Time: " + boardingPassDTO.getDepartureTime()), 155, 570, TextAlignment.LEFT);
            document.showTextAligned(new Paragraph("Arrival Time: " + boardingPassDTO.getArrivalTime()), 155, 550, TextAlignment.LEFT);
            document.showTextAligned(new Paragraph("PNR: " + boardingPassDTO.getPnr()), 155, 530, TextAlignment.LEFT);
            document.showTextAligned(new Paragraph("eTicketNumber: " + boardingPassDTO.getETicketNumber()), 155, 510, TextAlignment.LEFT);

            float ticketStatusXPosition = 425; // Adjust based on template layout

            document.showTextAligned(new Paragraph("Ticket Status: " + boardingPassDTO.getTicketStatus()), ticketStatusXPosition, 670, TextAlignment.LEFT);

            // Generate and add barcode to the PDF
            Image barcodeImage = generateBarcode(boardingPassDTO.getETicketNumber());

            if (barcodeImage == null) {
                throw new Exception("Barcode image generation failed");
            }

            // Position the barcode in the top-right corner
            float pageWidth = pdfDoc.getDefaultPageSize().getWidth();
            float barcodeWidth = barcodeImage.getImageWidth();
            float barcodeHeight = barcodeImage.getImageHeight();

            // Adjust the position for the top-right corner
            float xPosition = pageWidth - barcodeWidth - 50; // 50 points margin from the right
            float yPosition = 500; // Adjust to fit the document design

            barcodeImage.setFixedPosition(xPosition, yPosition);
            document.add(barcodeImage);

            // Close the document
            document.close();
            pdfReader.close();

        } catch (FileNotFoundException fnfe) {
            log.error("PDF Template file not found at: " + templatePath);
            throw fnfe;
        } catch (Exception e) {
            log.error("Error occurred while generating boarding pass: {}", e.getMessage(), e);
            throw e;
        }

        return byteArrayOutputStream; // Return the generated PDF as a ByteArrayOutputStream
    }


    // TODO: 10/1/2024 BARCODE GENERATOR

    private Image generateBarcode(String data) throws Exception {

        try {
            int width = 100;
            int height = 30;

            // Create the barcode writer for Code 128 format
            Code128Writer barcodeWriter = new Code128Writer();
            BitMatrix bitMatrix = barcodeWriter.encode(data, BarcodeFormat.CODE_128, width, height);

            // Define barcode and background colors

            int barcodeColor = 0xFF000000;    // Black
            int backgroundColor = 0xE5E5E5; // White

            MatrixToImageConfig config = new MatrixToImageConfig(barcodeColor, backgroundColor);

            // Generate barcode image and write it to a ByteArrayOutputStream
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream, config);
            byte[] pngData = pngOutputStream.toByteArray();

            // Convert PNG data into an iText Image object
            ImageData imageData = ImageDataFactory.create(pngData);
            return new Image(imageData);

        } catch (Exception e) {
            log.error("ERROR OCCURRED WHILE GENERATING BARCODE: {}", e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow the exception or return null if you want to handle it differently
        }
    }

    public String generatePassword() {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        int length = 8;

        StringBuilder otp = new StringBuilder();

        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());

            otp.append(characters.charAt(index));
        }

        return otp.toString();
    }

    // convert string minutes into duration

    public Duration convertStringToDuration(int totalMinutesString) {
        return Duration.ofMinutes(totalMinutesString);
    }
}
