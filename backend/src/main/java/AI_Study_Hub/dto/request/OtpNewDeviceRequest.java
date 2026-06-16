package AI_Study_Hub.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpNewDeviceRequest {
      String email;
      String otp;
      String deviceId;
}
