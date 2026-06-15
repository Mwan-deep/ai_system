package AI_Study_Hub.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "semesters")
@Data
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "semester_id")
    private Long semesterId;

    @Column(name = "semester_name")
    private String semesterName;
}