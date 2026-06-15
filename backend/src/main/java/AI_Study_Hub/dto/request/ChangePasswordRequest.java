package AI_Study_Hub.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ChangePasswordRequest {
    String oldPassword;
    @Size(min = 5 , message = "PASSWORD_INVALIDATION")
    String newPassword;
    @Size(min = 5 , message = "PASSWORD_INVALIDATION")
    String confirmNewPassword;
}
