package AI_Study_Hub.controller;

import AI_Study_Hub.entity.StudyMaterial;
import AI_Study_Hub.service.GoogleDriveService;
import AI_Study_Hub.service.StudyMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class StudyMaterialController {

    private final StudyMaterialService materialService;
    private final GoogleDriveService googleDriveService; // <--- THÊM DÒNG NÀY

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("userId") Long userId,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("semesterId") Long semesterId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file) {

        try {
            // Gọi Service để xử lý logic
            StudyMaterial savedMaterial = materialService.uploadDocument(
                    userId, subjectId, semesterId, title, description, file
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMaterial);

        } catch (Exception e) {
            // Trả về lỗi 400 nếu có bất kỳ trục trặc nào (thiếu user, lỗi file...)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi upload: " + e.getMessage());
        }
    }

    // 1. API: Xem danh sách toàn bộ tài liệu
    @GetMapping
    public ResponseEntity<List<StudyMaterial>> getAllDocuments() {
        return ResponseEntity.ok(materialService.getAllMaterials());
    }

    // 2. API: Xem chi tiết một tài liệu
    @GetMapping("/{id}")
    public ResponseEntity<StudyMaterial> getDocumentDetail(@PathVariable("id") Long id) {
        try {
            StudyMaterial material = materialService.getMaterialById(id);
            return ResponseEntity.ok(material);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // 3. API: Tải tài liệu về máy (Hỗ trợ Google Drive và Internet URL)
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadDocument(@PathVariable("id") Long id) {
        try {
            StudyMaterial material = materialService.getMaterialById(id);
            String fileIdOrUrl = material.getFileUrl();

            if (fileIdOrUrl == null || fileIdOrUrl.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài liệu không có file đính kèm!");
            }

            // TRƯỜNG HỢP 1: Nếu lưu link internet tự do (http/https)
            if (fileIdOrUrl.startsWith("http://") || fileIdOrUrl.startsWith("https://")) {
                org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(fileIdOrUrl);
                String contentType = material.getFileType() != null ? material.getFileType() : "application/octet-stream";
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + material.getFileName() + "\"")
                        .body(resource);
            }

            // TRƯỜNG HỢP 2: Nếu là File ID của Google Drive
            com.google.api.services.drive.Drive driveService = googleDriveService.getDriveService();

            // Yêu cầu Google Drive truyền luồng dữ liệu của file đó về server
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống khi tải file từ Google Drive: " + e.getMessage());
        }
    }

    // 4. API: Xóa tài liệu
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable("id") Long id) {
        try {
            materialService.deleteMaterial(id);
            return ResponseEntity.ok("Đã xóa tài liệu và file vật lý thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi xóa: " + e.getMessage());
        }
    }
    // 5. API: Tìm kiếm tài liệu (Chỉ cần 1 ô Keyword)
    @GetMapping("/search")
    public ResponseEntity<List<StudyMaterial>> searchDocuments(
            @RequestParam(value = "keyword", required = false) String keyword) {

        try {
            List<StudyMaterial> results = materialService.searchMaterials(keyword);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}