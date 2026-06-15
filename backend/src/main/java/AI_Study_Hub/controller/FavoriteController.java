package AI_Study_Hub.controller;

import AI_Study_Hub.entity.StudyMaterial;
import AI_Study_Hub.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 1. API: Thêm hoặc Bỏ yêu thích
    @PostMapping("/toggle")
    public ResponseEntity<String> toggleFavorite(
            @RequestParam("userId") Long userId,
            @RequestParam("materialId") Long materialId) {
        try {
            String resultMessage = favoriteService.toggleFavorite(userId, materialId);
            return ResponseEntity.ok(resultMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 2. API: Lấy danh sách tài liệu yêu thích của 1 User
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserFavorites(@PathVariable("userId") Long userId) {
        try {
            List<StudyMaterial> favoriteMaterials = favoriteService.getUserFavoriteMaterials(userId);
            return ResponseEntity.ok(favoriteMaterials);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}