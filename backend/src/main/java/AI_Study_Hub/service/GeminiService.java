package AI_Study_Hub.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // Sử dụng model gemini-1.5-flash-latest (phiên bản mới nhất được hỗ trợ)
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public String chatWithGemini(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        String url = GEMINI_API_URL + apiKey;

        // 1. Cấu hình Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. Xây dựng cấu trúc Body theo đúng chuẩn JSON
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> partsNode = new HashMap<>();
        partsNode.put("parts", Collections.singletonList(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(partsNode));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            // 3. Gửi Request sang Google
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            // 4. Bóc tách JSON bằng thư viện GSON (Hỗ trợ tốt nhất cho các dịch vụ của Google)
            JsonObject rootObject = JsonParser.parseString(response.getBody()).getAsJsonObject();

            return rootObject.getAsJsonArray("candidates").get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts").get(0).getAsJsonObject()
                    .get("text").getAsString();

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API Gemini: " + e.getMessage());
            return "Xin lỗi, AI hiện tại đang quá tải hoặc gặp sự cố kết nối. Vui lòng thử lại sau!";
        }
    }

    // Hàm chuyên dụng để phân tích tài liệu ngay khi vừa Upload
    public Map<String, String> analyzeDocument(String extractedText) {
        // Cắt bớt văn bản nếu quá dài (Gemini Flash xử lý tối ưu khoảng 20,000 từ một lúc)
        String textToAnalyze = extractedText.length() > 50000 ? extractedText.substring(0, 50000) : extractedText;

        String prompt = "Dưới đây là nội dung của một tài liệu học tập. " +
                "Bạn hãy thực hiện 2 nhiệm vụ sau:\n" +
                "1. Viết một đoạn tóm tắt ngắn gọn (khoảng 3-5 câu) về nội dung chính của tài liệu.\n" +
                "2. Rút trích ra 5 đến 10 từ khóa (keywords) quan trọng nhất của tài liệu, phân cách nhau bằng dấu phẩy.\n\n" +
                "Định dạng câu trả lời bắt buộc phải tuân theo cấu trúc sau (không có ký tự nào khác):\n" +
                "TÓM TẮT: [Nội dung tóm tắt của bạn]\n" +
                "TỪ KHÓA: [Danh sách từ khóa]\n\n" +
                "--- NỘI DUNG TÀI LIỆU ---\n" + textToAnalyze;

        String response = chatWithGemini(prompt);

        // Bóc tách kết quả AI trả về để đưa vào Database
        Map<String, String> result = new HashMap<>();
        try {
            String[] parts = response.split("TỪ KHÓA:");
            String summaryPart = parts[0].replace("TÓM TẮT:", "").trim();
            String keywordPart = parts[1].trim();

            result.put("summary", summaryPart);
            result.put("keywords", keywordPart);
        } catch (Exception e) {
            result.put("summary", "Không thể tự động tóm tắt do lỗi định dạng.");
            result.put("keywords", "AI, Analysis, Error");
        }

        return result;
    }
}