package AI_Study_Hub.service;

import AI_Study_Hub.repository.*;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.ChatMessage;
import AI_Study_Hub.entity.ChatSession;
import AI_Study_Hub.entity.StudyMaterial;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;
    private final StudyMaterialRepository materialRepository;
    private final MaterialContextRepository materialContextRepository;
    private final GeminiService geminiService;

    @Transactional
    public String processUserMessage(Long accountId, Long materialId, String prompt) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        StudyMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));

        // 1. Quản lý Phiên Chat
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
            session = existingSessions.get(0);
        }

        // 2. Lưu tin nhắn của người dùng
        ChatMessage userMessage = ChatMessage.builder()
                .chatSession(session)
                .senderRole("USER")
                .messageContent(prompt)
                .build();
        chatMessageRepository.save(userMessage);

        // 3. LẤY NGỮ CẢNH TÀI LIỆU (Tương lai sẽ dùng Vector Search thay vì nạp toàn bộ text)
        String documentContent = "Tài liệu này hiện chưa có nội dung văn bản được trích xuất.";
        var materialContext = materialContextRepository.findByStudyMaterial_MaterialId(materialId);
        if (materialContext.isPresent() && materialContext.get().getExtractedText() != null) {
            // TODO: Về sau, chỗ này sẽ gọi thuật toán Semantic Search (như Milvus, Pinecone, hoặc PGVector)
            // để lấy ra Top 3 đoạn văn có liên quan nhất với "prompt" thay vì lấy sạch text.
            documentContent = materialContext.get().getExtractedText();
        }

        // 4. LẤY TRÍ NHỚ HỘI THOẠI (5 tin nhắn gần nhất)
        List<ChatMessage> chatHistory = chatMessageRepository
                .findTop5ByChatSession_SessionIdOrderByCreatedAtDesc(session.getSessionId());

        // Đảo ngược danh sách để tin nhắn cũ nằm trên, tin mới nằm dưới
        Collections.reverse(chatHistory);

        StringBuilder historyBuilder = new StringBuilder();
        for (ChatMessage msg : chatHistory) {
            // Không nối lại chính câu hỏi vừa lưu ở trên
            if (!msg.getMessageId().equals(userMessage.getMessageId())) {
                historyBuilder.append(msg.getSenderRole()).append(": ").append(msg.getMessageContent()).append("\n");
            }
        }

        // 5. CẤU TRÚC PROMPT MỚI
        String systemInstruction =
                "Bạn là trợ lý học tập AI của hệ thống AI Study Hub. " +
                        "Nhiệm vụ của bạn là trả lời câu hỏi DỰA TRÊN nội dung tài liệu và LỊCH SỬ trò chuyện.\n\n" +
                        "--- NỘI DUNG TÀI LIỆU (" + material.getTitle() + ") ---\n" +
                        documentContent + "\n" +
                        "-----------------------------------\n\n" +
                        "--- LỊCH SỬ TRÒ CHUYỆN GẦN ĐÂY ---\n" +
                        (historyBuilder.isEmpty() ? "Chưa có cuộc trò chuyện nào trước đó.\n" : historyBuilder.toString()) +
                        "-----------------------------------\n\n" +
                        "USER: " + prompt + "\n" +
                        "AI: ";

        // 6. Gửi cho AI và lưu kết quả
        String aiResponse = geminiService.chatWithGemini(systemInstruction);

        ChatMessage aiMessage = ChatMessage.builder()
                .chatSession(session)
                .senderRole("AI")
                .messageContent(aiResponse)
                .build();
        chatMessageRepository.save(aiMessage);

        return aiResponse;
    }
}