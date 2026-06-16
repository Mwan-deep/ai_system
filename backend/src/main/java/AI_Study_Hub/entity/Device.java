package AI_Study_Hub.entity;

import com.google.api.client.util.DateTime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String deviceId;
    boolean trusted;
    LocalDateTime lastLogin;
    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;
}
