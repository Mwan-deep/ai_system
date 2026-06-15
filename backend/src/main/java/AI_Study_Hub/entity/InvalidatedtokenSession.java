package AI_Study_Hub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "invalidatedToken_session")
public class InvalidatedtokenSession {
    @Id
    @Size(max = 255)
    @Column(name = "id", nullable = false)
    String invalidId;

    @Column(name = "expireTime")
    LocalDateTime expireTime;


}