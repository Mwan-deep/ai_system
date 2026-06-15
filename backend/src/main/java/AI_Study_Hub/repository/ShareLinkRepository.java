package AI_Study_Hub.repository;

import AI_Study_Hub.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {

    // Tìm kiếm một record chia sẻ dựa vào chuỗi Token người dùng nhập
    Optional<ShareLink> findByShareToken(String shareToken);
}