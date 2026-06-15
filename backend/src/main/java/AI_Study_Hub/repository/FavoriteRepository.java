package AI_Study_Hub.repository;

import AI_Study_Hub.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Đã đổi từ User sang Account
    Optional<Favorite> findByAccount_AccountIdAndStudyMaterial_MaterialId(Long accountId, Long materialId);

    // Đã đổi từ User sang Account
    List<Favorite> findByAccount_AccountId(Long accountId);
}