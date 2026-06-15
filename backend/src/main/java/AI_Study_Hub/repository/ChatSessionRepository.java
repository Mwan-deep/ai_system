package AI_Study_Hub.repository;

import AI_Study_Hub.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    // Lấy toàn bộ lịch sử các phiên chat của 1 người dùng đối với 1 tài liệu cụ thể
    List<ChatSession> findByAccount_AccountIdAndStudyMaterial_MaterialId(Long accountId, Long materialId);
}