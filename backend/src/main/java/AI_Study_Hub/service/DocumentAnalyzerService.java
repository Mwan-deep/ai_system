package AI_Study_Hub.service;

import AI_Study_Hub.service.GeminiService;
import AI_Study_Hub.entity.MaterialContext;
import AI_Study_Hub.entity.StudyMaterial;
import AI_Study_Hub.repository.MaterialContextRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentAnalyzerService {

    private final MaterialContextRepository contextRepository;
    private final GeminiService geminiService;

    // Hàm chính: Xử lý toàn bộ quy trình
    public void processAndSaveContext(StudyMaterial material, MultipartFile file) {
        try {
            // 1. Trích xuất chữ (Extract)
            String extractedText = extractTextFromFile(file);

            if (extractedText == null || extractedText.trim().isEmpty()) {
                saveFallbackContext(material, "Tài liệu này không chứa văn bản có thể đọc được (có thể là file ảnh hoặc file rỗng).", "failed");
                return;
            }

            // 2. Gọi AI phân tích (Transform)
            Map<String, String> analysisResult = geminiService.analyzeDocument(extractedText);

            // 3. Lưu vào Database (Load)
            MaterialContext context = MaterialContext.builder()
                    .studyMaterial(material)
                    .extractedText(extractedText)
                    .summary(analysisResult.get("summary"))
                    .extractedKeywords(analysisResult.get("keywords"))
                    .embeddingStatus("completed")
                    .build();

            contextRepository.save(context);

        } catch (Exception e) {
            System.err.println("Lỗi khi phân tích tài liệu ID " + material.getMaterialId() + ": " + e.getMessage());
            saveFallbackContext(material, "Quá trình đọc file gặp lỗi: " + e.getMessage(), "failed");
        }
    }

    // Hàm phụ: Dùng "Mắt thần" PDFBox và POI để lấy chữ ra khỏi file
    private String extractTextFromFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        try (InputStream is = file.getInputStream()) {
            if (fileName.endsWith(".pdf")) {
                // SỬ DỤNG Loader.loadPDF CHO BẢN PDFBOX 3.0+
                try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(document);
                }
            } else if (fileName.endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(is);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    return extractor.getText();
                }
            } else if (fileName.endsWith(".txt")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        }
        return ""; // Trả về rỗng nếu định dạng không được hỗ trợ
    }

    // Hàm phụ: Lưu trạng thái lỗi nếu file hỏng hoặc không đọc được
    private void saveFallbackContext(StudyMaterial material, String reason, String status) {
        MaterialContext context = MaterialContext.builder()
                .studyMaterial(material)
                .extractedText(reason)
                .summary("Không có dữ liệu tóm tắt.")
                .extractedKeywords("N/A")
                .embeddingStatus(status)
                .build();
        contextRepository.save(context);
    }
}