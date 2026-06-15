package AI_Study_Hub.repository;

import AI_Study_Hub.entity.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {

    // Spring Boot quá thông minh: Bạn chỉ cần gõ tên hàm chuẩn, nó tự sinh ra câu query SQL!
    // VD: Tìm tất cả tài liệu theo ID môn học
    List<StudyMaterial> findBySubject_SubjectId(Long subjectId);

    // Tìm tất cả tài liệu public
    List<StudyMaterial> findByVisibility(String visibility);

    // Tính năng Tìm kiếm đa năng (Quét Tên tài liệu, Mô tả, Tên môn học, Mã môn học)
    @Query("SELECT s FROM StudyMaterial s WHERE " +
            ":keyword IS NULL OR " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.subject.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.subject.subjectCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<StudyMaterial> searchMaterials(@Param("keyword") String keyword);
}