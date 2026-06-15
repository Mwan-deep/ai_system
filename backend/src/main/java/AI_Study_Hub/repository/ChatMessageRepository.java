package AI_Study_Hub.repository;

import AI_Study_Hub.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Lấy toàn bộ tin nhắn trong 1 phiên chat, sắp xếp theo thời gian gửi (cũ nhất hiện trước)
    List<ChatMessage> findByChatSession_SessionIdOrderByCreatedAtAsc(Long sessionId);
}