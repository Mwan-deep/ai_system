package AI_Study_Hub.repository;

import AI_Study_Hub.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRespository extends JpaRepository<Role, String> {
    Role findRoleByRoleId(String roleId);
}
