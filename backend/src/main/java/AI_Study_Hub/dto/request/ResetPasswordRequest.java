package AI_Study_Hub.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    String gmail;
    String otp;
    @Size(min = 5, message = "PASSWORD_INVALIDATION")
    String newPassword;
    @Size(min = 5, message = "PASSWORD_INVALIDATION")
    String confirmPassword;
}
