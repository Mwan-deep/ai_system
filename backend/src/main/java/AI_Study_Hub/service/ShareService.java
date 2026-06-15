package AI_Study_Hub.service;

import AI_Study_Hub.entity.ShareLink;
import AI_Study_Hub.entity.StudyMaterial;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.repository.ShareLinkRepository;
import AI_Study_Hub.repository.StudyMaterialRepository;
import AI_Study_Hub.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final StudyMaterialRepository materialRepository;
    private final AccountRepository accountRepository; // Sử dụng AccountRepository

    // 1. Hàm tạo mã chia sẻ mới
    @Transactional
    public String generateShareLink(Long materialId, Long accountId, Integer expireDays) {
        StudyMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        // Sinh ra mã Token độc nhất bằng thuật toán UUID
        String token = UUID.randomUUID().toString();

        ShareLink shareLink = ShareLink.builder()
                .studyMaterial(material)
                .account(account) // Đã đổi thành account
                .shareToken(token)
                // Đã đổi biến thành expiredAt cho khớp với DB mới
                .expiredAt(expireDays != null ? LocalDateTime.now().plusDays(expireDays) : null)
                .build();

        shareLinkRepository.save(shareLink);
        return token;
    }

    // 2. Hàm xác thực Token khi có người bấm vào link tải
    public StudyMaterial validateTokenAndGetMaterial(String token) {
        ShareLink shareLink = shareLinkRepository.findByShareToken(token)
                .orElseThrow(() -> new RuntimeException("Link chia sẻ không tồn tại hoặc sai mã Token!"));

        // Kiểm tra xem link có bị hết hạn không
        if (shareLink.getExpiredAt() != null && shareLink.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link chia sẻ này đã hết hạn!");
        }

        return shareLink.getStudyMaterial();
    }
}