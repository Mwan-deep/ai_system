package AI_Study_Hub.dto.response;

import AI_Study_Hub.entity.Role;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class AccountResponse {
    Long id;
    String userName;
    String passwordHash;
    String fullName;
    String email;
    LocalDate dob;
    String gender;
    String avatarUrl;
    String bio;
    Set<Role> role;
}
