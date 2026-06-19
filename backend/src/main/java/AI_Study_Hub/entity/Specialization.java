package AI_Study_Hub.entity;

import jakarta.persistence.*;
import lombok.*;
// Thêm thư viện này ở phần import
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "specializations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "specialization_id")
    private Long specializationId;

    // Quan hệ N-1: Trỏ khóa ngoại major_id về bảng majors
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    @JsonIgnore // <--- BẮT BUỘC THÊM DÒNG NÀY ĐỂ NGẮT VÒNG LẶP JSON
    private Major major;

    @Column(name = "specialization_name", nullable = false, length = 100)
    private String specializationName;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

}