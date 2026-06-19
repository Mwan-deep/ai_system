package AI_Study_Hub.controller;

import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.StudyMaterial;
import AI_Study_Hub.repository.AccountRepository;
import AI_Study_Hub.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final AccountRepository accountRepository;

    // 1. API: Thêm hoặc Bỏ yêu thích (Đã bảo mật)
    @PostMapping("/toggle")
    public ResponseEntity<String> toggleFavorite(@RequestParam("materialId") Long materialId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account currentUser = accountRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi định danh"));

            String resultMessage = favoriteService.toggleFavorite(currentUser.getAccountId(), materialId);
            return ResponseEntity.ok(resultMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 2. API: Lấy danh sách tài liệu yêu thích của chính User đang đăng nhập
    @GetMapping("/my-favorites")
    public ResponseEntity<?> getUserFavorites() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account currentUser = accountRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi định danh"));

            List<StudyMaterial> favoriteMaterials = favoriteService.getUserFavoriteMaterials(currentUser.getAccountId());
            return ResponseEntity.ok(favoriteMaterials);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}