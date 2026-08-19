package com.skillbridge.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class PdfService {

    public String extractText(String filePath) throws IOException {

        File file = new File(filePath);

        if (!file.exists()) {
            throw new IOException("PDF file not found: " + filePath);
        }

        if (file.length() == 0) {
            throw new IOException("PDF file is empty (0 bytes)");
        }

        try (PDDocument document = Loader.loadPDF(file)) {

            int pageCount = document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String rawText = stripper.getText(document);
            String text = rawText != null ? rawText.trim() : "";

            System.out.println("========== PDF EXTRACTION ==========");
            System.out.println("FILE NAME: " + file.getName());
            System.out.println("FILE SIZE: " + file.length() + " bytes");
            System.out.println("PAGES: " + pageCount);
            System.out.println("PDF TEXT LENGTH: " + text.length());
            System.out.println("====================================");

            if (text.isBlank() || text.length() < 10) {
                throw new IOException(
                        "Could not extract text from PDF. The PDF may be scanned/image-based (OCR required)."
                );
            }

            return text;
        } catch (IOException e) {
            System.err.println("PDFBox extraction exception: " + e.getMessage());
            throw e;
        }
    }
}
