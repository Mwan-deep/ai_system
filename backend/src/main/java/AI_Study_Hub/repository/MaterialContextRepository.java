package AI_Study_Hub.repository;

import AI_Study_Hub.entity.MaterialContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaterialContextRepository extends JpaRepository<MaterialContext, Long> {
    Optional<MaterialContext> findByStudyMaterial_MaterialId(Long materialId);
}