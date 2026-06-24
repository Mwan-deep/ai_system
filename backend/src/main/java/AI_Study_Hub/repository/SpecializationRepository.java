package AI_Study_Hub.repository;

import AI_Study_Hub.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    // Câu lệnh JPA tự động sinh SQL: SELECT * FROM specializations WHERE major_id = ?
    List<Specialization> findByMajor_MajorId(Long majorId);
}