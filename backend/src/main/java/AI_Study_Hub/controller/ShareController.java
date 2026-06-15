package AI_Study_Hub.controller;

import AI_Study_Hub.entity.StudyMaterial;
import AI_Study_Hub.service.GoogleDriveService;
import AI_Study_Hub.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;
    private final GoogleDriveService googleDriveService;

    // 1. API: Chủ tài liệu yêu cầu tạo Link chia sẻ
    @PostMapping("/generate")
    public ResponseEntity<?> generateLink(
            @RequestParam("materialId") Long materialId,
            @RequestParam("accountId") Long accountId, // Cập nhật thành accountId
            @RequestParam(value = "expireDays", required = false) Integer expireDays) {
        try {
            String token = shareService.generateShareLink(materialId, accountId, expireDays);

            // Ghép Token vào một URL hoàn chỉnh
            String shareUrl = "http://localhost:8080/api/v1/share/download/" + token;

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("shareUrl", shareUrl);
            response.put("message", "Tạo link chia sẻ thành công!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 2. API: Người lạ dùng Link để tải tài liệu ẩn danh
    @GetMapping("/download/{token}")
    public ResponseEntity<?> downloadSharedDocument(@PathVariable("token") String token) {
        try {
            StudyMaterial material = shareService.validateTokenAndGetMaterial(token);
            String fileIdOrUrl = material.getFileUrl();

            if (fileIdOrUrl == null || fileIdOrUrl.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài liệu không có file đính kèm!");
            }

            if (fileIdOrUrl.startsWith("http://") || fileIdOrUrl.startsWith("https://")) {
                org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(fileIdOrUrl);
                String contentType = material.getFileType() != null ? material.getFileType() : "application/octet-stream";
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + material.getFileName() + "\"")
                        .body(resource);
            }

            com.google.api.services.drive.Drive driveService = googleDriveService.getDriveService();
            java.io.InputStream inputStream = driveService.files().get(fileIdOrUrl).executeMediaAsInputStream();
            org.springframework.core.io.InputStreamResource resource = new org.springframework.core.io.InputStreamResource(inputStream);

            String contentType = material.getFileType() != null ? material.getFileType() : "application/octet-stream";
            String downloadFileName = material.getFileName() != null ? material.getFileName() : "downloaded_document.pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể truy cập link: " + e.getMessage());
        }
    }
}