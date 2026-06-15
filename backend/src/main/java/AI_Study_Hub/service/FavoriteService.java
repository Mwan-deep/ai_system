package AI_Study_Hub.service;

import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.Favorite;
import AI_Study_Hub.entity.StudyMaterial;
import AI_Study_Hub.repository.AccountRepository;
import AI_Study_Hub.repository.FavoriteRepository;
import AI_Study_Hub.repository.StudyMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AccountRepository accountRepository; // Đã đổi thành AccountRepository
    private final StudyMaterialRepository materialRepository;

    @Transactional
    public String toggleFavorite(Long accountId, Long materialId) { // Đổi tham số thành accountId

        // Gọi hàm mới bằng chữ "Account"
        Optional<Favorite> existingFavorite = favoriteRepository.findByAccount_AccountIdAndStudyMaterial_MaterialId(accountId, materialId);

        if (existingFavorite.isPresent()) {
            favoriteRepository.delete(existingFavorite.get());
            return "Đã bỏ yêu thích tài liệu!";
        } else {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Tài khoản"));
            StudyMaterial material = materialRepository.findById(materialId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Tài liệu"));

            Favorite newFavorite = Favorite.builder()
                    .account(account) // Dùng biến account thay vì user
                    .studyMaterial(material)
                    .build();

            favoriteRepository.save(newFavorite);
            return "Đã thêm tài liệu vào danh sách yêu thích!";
        }
    }

    public List<StudyMaterial> getUserFavoriteMaterials(Long accountId) {
        // Gọi hàm mới bằng chữ "Account"
        List<Favorite> favorites = favoriteRepository.findByAccount_AccountId(accountId);

        return favorites.stream()
                .map(Favorite::getStudyMaterial)
                .collect(Collectors.toList());
    }
}