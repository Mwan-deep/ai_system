package AI_Study_Hub.repository;

import AI_Study_Hub.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Hàm này giúp Controller và Security tìm người dùng qua tên đăng nhập (hết báo đỏ)
    Optional<Account> findByUserName(String userName);

    // Hai hàm này để dành cho API Đăng ký (Register) kiểm tra xem tên đăng nhập hoặc email đã bị ai khác lấy chưa
    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);
}