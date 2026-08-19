package com.skillbridge.service;

import com.skillbridge.entity.Resume;
import com.skillbridge.entity.User;
import com.skillbridge.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private SkillExtractionService skillExtractionService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public Resume uploadResume(User user, MultipartFile file) throws IOException {

        // 1. Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file cannot be empty");
        }

        System.out.println("UPLOAD API HIT");
        System.out.println("FILE NAME: " + file.getOriginalFilename());
        System.out.println("FILE SIZE: " + file.getSize() + " bytes");

        // 2. Validate PDF format
        String contentType = file.getContentType();
        String originalFileName = file.getOriginalFilename();

        boolean isPdfMime = contentType != null && contentType.equalsIgnoreCase("application/pdf");
        boolean isPdfExt = originalFileName != null && originalFileName.toLowerCase().endsWith(".pdf");

        if (!isPdfMime && !isPdfExt) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String fileName = originalFileName.replace(" ", "_");

        // 3. Save PDF using fileStorageService abstraction
        String filePath = fileStorageService.storeFile(file, fileName);

        System.out.println("PDF SAVED: " + filePath);

        // 5. Extract text using PDFBox
        String extractedText = pdfService.extractText(filePath);

        System.out.println("PDF TEXT EXTRACTED");
        System.out.println("TEXT LENGTH: " + (extractedText != null ? extractedText.length() : 0));
        if (extractedText != null && !extractedText.isBlank()) {
            String preview = extractedText.substring(0, Math.min(200, extractedText.length())).replace("\n", " ");
            System.out.println("Extracted text snippet (first 200 chars): " + preview);
        }

        // 6. Validate extracted text BEFORE calling Gemini
        if (extractedText == null || extractedText.isBlank() || extractedText.trim().length() < 10) {
            System.err.println("EXTRACTION FAILED: Extracted text is empty or suspiciously short.");
            throw new IllegalArgumentException("Could not extract text from PDF. The PDF may be scanned/image-based (OCR required).");
        }

        // 7. Send resume text to Gemini
        System.out.println("SENDING RESUME TO GEMINI...");
        String aiAnalysis = geminiService.analyzeResume(extractedText);
        System.out.println("GEMINI ANALYSIS RECEIVED");

        // 8. Find existing resume or create new one
        Resume resume = resumeRepository.findByUser(user)
                .orElse(new Resume());

        // 9. Save resume information
        resume.setFileName(fileName);
        resume.setFilePath(filePath);
        resume.setExtractedText(extractedText);
        resume.setAiAnalysis(aiAnalysis);
        resume.setUser(user);

        // 10. Save Resume entity to database
        Resume savedResume = resumeRepository.save(resume);
        System.out.println("RESUME DATABASE SAVE SUCCESS");

        // 11. Extract and save user skills into database
        skillExtractionService.extractAndSaveSkills(user, extractedText);
        System.out.println("SKILLS SAVE SUCCESS");
        System.out.println("===========================================");

        return savedResume;
    }

    public Resume getResume(User user) {
        return resumeRepository.findByUser(user).orElse(null);
    }
}