package AI_Study_Hub.service;

import AI_Study_Hub.entity.*;
import AI_Study_Hub.repository.SemesterRepository;
import AI_Study_Hub.repository.StudyMaterialRepository;
import AI_Study_Hub.repository.SubjectRepository;
import AI_Study_Hub.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyMaterialService {

    // Lombok @RequiredArgsConstructor sẽ tự động inject các Repository này vào
    private final StudyMaterialRepository materialRepository;
    private final AccountRepository accountRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final GoogleDriveService googleDriveService; // <--- THÊM DÒNG NÀY
    private final DocumentAnalyzerService documentAnalyzerService;

    // Đường dẫn lưu file tương đối (sẽ tạo thư mục 'uploads' trong thư mục project)
    private final String UPLOAD_DIR = "uploads/";

    @Transactional
    public StudyMaterial uploadDocument(Long userId, Long subjectId, Long semesterId,
                                        String title, String description, MultipartFile file) throws IOException {

        // 1. Kiểm tra tồn tại
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Subject"));
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Semester"));

        // 2. Tự động đẩy file lên Google Drive và lấy ID về
        String googleDriveFileId = googleDriveService.uploadFileToDrive(file);

        // 3. Lưu thông tin cơ bản vào Database
        StudyMaterial material = StudyMaterial.builder()
                .account(account) // Đã đổi từ user -> account
                .subject(subject)
                .semester(semester)
                .title(title)
                .description(description)
                .fileUrl(googleDriveFileId)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .visibility("PRIVATE")
                .viewCount(0)
                .downloadCount(0)
                .build();

        StudyMaterial savedMaterial = materialRepository.save(material);

        // 4. KÍCH HOẠT HỆ THỐNG PHÂN TÍCH TÀI LIỆU TỰ ĐỘNG
        // Giao file cho DocumentAnalyzerService để bóc tách chữ và đưa cho AI phân tích
        documentAnalyzerService.processAndSaveContext(savedMaterial, file);

        return savedMaterial;
    }

    // 1. Lấy toàn bộ danh sách tài liệu
    public List<StudyMaterial> getAllMaterials() {
        return materialRepository.findAll();
    }

    // 2. Lấy chi tiết một tài liệu theo ID
    public StudyMaterial getMaterialById(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu với ID: " + materialId));
    }

    @Transactional
    public void deleteMaterial(Long materialId) throws IOException {
        StudyMaterial material = getMaterialById(materialId);
        String fileUrlOrId = material.getFileUrl();

        // Nếu có ID file (không rỗng và không phải là link http thông thường)
        if (fileUrlOrId != null && !fileUrlOrId.isEmpty() && !fileUrlOrId.startsWith("http")) {
            // Gọi Google Drive Service để xóa file trên mây
            googleDriveService.deleteFileFromDrive(fileUrlOrId);
        }

        // Xóa thông tin trong SQL Server
        materialRepository.delete(material);
    }

    // 4. Tìm kiếm tài liệu đa năng
    public List<StudyMaterial> searchMaterials(String keyword) {
        return materialRepository.searchMaterials(keyword);
    }

}