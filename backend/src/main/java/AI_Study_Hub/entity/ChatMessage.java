package AI_Study_Hub.entity;

import AI_Study_Hub.entity.ChatSession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    // Liên kết với Phiên chat (Session)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    // Cờ đánh dấu ai là người gửi (USER hoặc AI)
    @Column(name = "sender_role", nullable = false)
    private String senderRole;

    @Column(name = "message_content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String messageContent;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "token_used")
    private Integer tokenUsed;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.messageType == null) {
            this.messageType = "text";
        }
        if (this.tokenUsed == null) {
            this.tokenUsed = 0;
        }
    }
}