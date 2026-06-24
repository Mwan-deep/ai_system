package AI_Study_Hub.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subjects")
@Getter
@Setter // <--- Bắt buộc phải có để sinh ra hàm setSpecialization()
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Long subjectId;

    // ĐÂY LÀ ĐOẠN QUAN TRỌNG NHẤT: MAP KHÓA NGOẠI VỚI BẢNG SPECIALIZATION
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;

    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName;

    @Column(name = "subject_code", length = 20)
    private String subjectCode;

    @Column(name = "description", columnDefinition = "VARCHAR(MAX)")
    private String description;
}