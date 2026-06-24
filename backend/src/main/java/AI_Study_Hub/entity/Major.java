package AI_Study_Hub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "majors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "major_id")
    private Long majorId;

    @Column(name = "major_name", nullable = false, length = 100)
    private String majorName;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    // Quan hệ 1-N: 1 Chuyên ngành lớn có nhiều Chuyên ngành hẹp
    // fetch = FetchType.LAZY để tối ưu hiệu suất, chỉ query khi cần
    @OneToMany(mappedBy = "major", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Specialization> specializations;
}