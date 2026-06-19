package AI_Study_Hub.repository;

import AI_Study_Hub.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    // Tìm môn học dựa trên Tên môn và ID Chuyên ngành hẹp
    Optional<Subject> findBySubjectNameAndSpecialization_SpecializationId(String subjectName, Long specializationId);
}