package AI_Study_Hub.controller;

import AI_Study_Hub.entity.Major;
import AI_Study_Hub.entity.Semester;
import AI_Study_Hub.entity.Specialization;
import AI_Study_Hub.repository.MajorRepository;
import AI_Study_Hub.repository.SemesterRepository;
import AI_Study_Hub.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    private final MajorRepository majorRepository;
    private final SpecializationRepository specializationRepository;
    private final SemesterRepository semesterRepository;

    // API 1: Lấy toàn bộ Chuyên ngành lớn
    @GetMapping("/majors")
    public ResponseEntity<List<Major>> getAllMajors() {
        return ResponseEntity.ok(majorRepository.findAll());
    }

    // API 2: Lấy Chuyên ngành hẹp dựa theo ID của Chuyên ngành lớn
    // Frontend gọi API này ngay sau khi người dùng chọn xong Major
    @GetMapping("/majors/{majorId}/specializations")
    public ResponseEntity<List<Specialization>> getSpecializationsByMajor(@PathVariable Long majorId) {
        return ResponseEntity.ok(specializationRepository.findByMajor_MajorId(majorId));
    }

    // API 3: Lấy toàn bộ Kỳ học
    @GetMapping("/semesters")
    public ResponseEntity<List<Semester>> getAllSemesters() {
        return ResponseEntity.ok(semesterRepository.findAll());
    }
}