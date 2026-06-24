package AI_Study_Hub.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadMaterialRequest {
    private Long specializationId; // Frontend gửi lên sau khi chọn Dropdown
    private Long semesterId;       // Frontend gửi lên sau khi chọn Dropdown
    private String subjectName;    // Frontend gửi lên dạng Text (Sinh viên tự gõ)

    private String title;          // Tiêu đề tài liệu
    private String description;    // Mô tả
    private String visibility;     // 'PUBLIC' hoặc 'PRIVATE'

    private MultipartFile file;    // File PDF/Word
}