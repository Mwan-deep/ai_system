package AI_Study_Hub.service;

import AI_Study_Hub.repository.*;
import AI_Study_Hub.service.GeminiService;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.ChatMessage;
import AI_Study_Hub.entity.ChatSession;
import AI_Study_Hub.entity.StudyMaterial;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;
    private final StudyMaterialRepository materialRepository;
    private final MaterialContextRepository materialContextRepository; // <--- THÊM DÒNG NÀY
    private final GeminiService geminiService; // Gọi trạm trung chuyển AI vào đây

    @Transactional
    public String processUserMessage(Long accountId, Long materialId, String prompt) {
        // 1. Xác thực người dùng và tài liệu
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        StudyMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));

        // 2. Tự động Tìm hoặc Tạo mới Phiên chat (Mỗi tài liệu có 1 phiên chat riêng biệt)
        List<ChatSession> existingSessions = chatSessionRepository
                .findByAccount_AccountIdAndStudyMaterial_MaterialId(accountId, materialId);

        ChatSession session;
        if (existingSessions.isEmpty()) {
            session = ChatSession.builder()
                    .account(account)
                    .studyMaterial(material)
                    .title("Hỏi đáp tài liệu: " + material.getTitle())
                    .build();
            session = chatSessionRepository.save(session);
        } else {
            session = existingSessions.get(0); // Nếu đã từng chat thì lấy lại lịch sử cũ
        }

        // 3. LƯU LỊCH SỬ: Ghi lại tin nhắn của người dùng
        ChatMessage userMessage = ChatMessage.builder()
                .chatSession(session)
                .senderRole("USER")
                .messageContent(prompt)
                .build();
        chatMessageRepository.save(userMessage);

        // 4. KỸ NGHỆ CÂU LỆNH (Kiến trúc RAG): Bơm bối cảnh vào cho AI
        String documentContent = "Tài liệu này hiện chưa có nội dung văn bản được trích xuất.";

        // Tìm xem tài liệu này đã được trích xuất chữ (OCR) lưu vào DB chưa
        var materialContext = materialContextRepository.findByStudyMaterial_MaterialId(materialId);
        if (materialContext.isPresent() && materialContext.get().getExtractedText() != null) {
            documentContent = materialContext.get().getExtractedText();
        }

        String systemInstruction =
                "Bạn là trợ lý học tập AI của hệ thống AI Study Hub. " +
                        "Nhiệm vụ của bạn là trả lời câu hỏi của sinh viên DỰA TRÊN nội dung tài liệu được cung cấp dưới đây. " +
                        "Tuyệt đối không bịa đặt thông tin. Nếu câu trả lời không có trong tài liệu, hãy nói rõ là tài liệu không đề cập đến.\n\n" +
                        "--- NỘI DUNG TÀI LIỆU (" + material.getTitle() + ") ---\n" +
                        documentContent + "\n" +
                        "-----------------------------------\n\n" +
                        "Câu hỏi của sinh viên: " + prompt;

        // 5. Gửi câu hỏi (đã được bọc thêm kỷ luật) cho Gemini
        String aiResponse = geminiService.chatWithGemini(systemInstruction);

        // 6. LƯU LỊCH SỬ: Ghi lại câu trả lời của AI
        ChatMessage aiMessage = ChatMessage.builder()
                .chatSession(session)
                .senderRole("AI")
                .messageContent(aiResponse)
                .build();
        chatMessageRepository.save(aiMessage);

        // 7. Trả kết quả về cho Frontend hiển thị
        return aiResponse;
    }
}