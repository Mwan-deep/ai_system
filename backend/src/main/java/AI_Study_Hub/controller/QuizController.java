package AI_Study_Hub.controller;

import AI_Study_Hub.dto.QuizResponseDTO;
import AI_Study_Hub.dto.QuizSubmitRequest;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.Quiz;
import AI_Study_Hub.entity.QuizAttempt;
import AI_Study_Hub.repository.AccountRepository;
import AI_Study_Hub.service.QuizGeneratorService;
import AI_Study_Hub.service.QuizGradingService;
import AI_Study_Hub.service.QuizQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizGeneratorService quizGeneratorService;
    private final QuizGradingService quizGradingService;
    // Khai báo thêm ở đầu class Controller
    private final QuizQueryService quizQueryService;
    private final AccountRepository accountRepository;

    // 1. API Sinh đề thi (Đã bảo mật)
    @PostMapping("/generate")
    public ResponseEntity<?> generateQuiz(
            @RequestParam("materialId") Long materialId,
            @RequestParam(value = "quantity", defaultValue = "5") int quantity) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account currentUser = accountRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi định danh"));

            Quiz quiz = quizGeneratorService.generateQuizFromMaterial(materialId, currentUser.getAccountId(), quantity);

            Map<String, Object> response = new HashMap<>();
            response.put("quizId", quiz.getQuizId());
            response.put("title", quiz.getTitle());
            response.put("quantity", quiz.getQuantity());
            response.put("message", "Hệ thống AI đã thiết kế đề thi trắc nghiệm và lưu thành công!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi sinh đề thi: " + e.getMessage());
        }
    }

    // 2. API Nộp bài và Chấm điểm (THÊM MỚI)
    @PostMapping("/submit")
    public ResponseEntity<?> submitAndGradeQuiz(@RequestBody QuizSubmitRequest request) {
        try {
            QuizAttempt attempt = quizGradingService.gradeQuiz(request);

            Map<String, Object> response = new HashMap<>();
            response.put("attemptId", attempt.getAttemptId());
            response.put("score", attempt.getScore());
            response.put("totalWrong", attempt.getTotalQuestionFalse());
            response.put("message", "Nộp bài và chấm điểm thành công!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi chấm điểm: " + e.getMessage());
        }
    }

    // 3. API: Frontend gọi để lấy đề thi về hiển thị cho sinh viên (Đã bảo mật)
    @GetMapping("/{quizId}/take")
    public ResponseEntity<?> getQuizForTaking(@PathVariable("quizId") Long quizId) {
        try {
            QuizResponseDTO quizData = quizQueryService.getQuizForTaking(quizId);
            return ResponseEntity.ok(quizData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi truy xuất bài thi: " + e.getMessage());
        }
    }
}