package AI_Study_Hub.repository;

import AI_Study_Hub.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    // Tìm toàn bộ lịch sử làm bài của một học sinh
    List<QuizAttempt> findByAccount_AccountId(Long accountId);
}