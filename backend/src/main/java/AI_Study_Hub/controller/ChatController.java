package AI_Study_Hub.controller;

import AI_Study_Hub.entity.Account;
import AI_Study_Hub.repository.AccountRepository;
import AI_Study_Hub.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AccountRepository accountRepository;


    // API: Gửi câu hỏi cho AI (Có lưu lịch sử)
    @PostMapping("/ask")
    public ResponseEntity<?> askAI(
            @RequestParam("materialId") Long materialId,
            @RequestParam("prompt") String prompt) {

        if (prompt == null || prompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Bạn chưa nhập câu hỏi!");
        }

        try {
            // Bảo mật JWT
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account currentUser = accountRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi định danh"));

            String aiResponse = chatService.processUserMessage(currentUser.getAccountId(), materialId, prompt);

            Map<String, Object> response = new HashMap<>();
            response.put("materialId", materialId);
            response.put("question", prompt);
            response.put("answer", aiResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi hệ thống Chat: " + e.getMessage());
        }
    }
}