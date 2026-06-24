package AI_Study_Hub.controller;

import AI_Study_Hub.repository.StudyMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final StudyMaterialRepository studyMaterialRepository;

    // API: Vinh danh tài liệu được tải nhiều nhất
    @GetMapping("/top-downloads")
    public ResponseEntity<?> getTopDownloads() {
        return ResponseEntity.ok(studyMaterialRepository.findTop10ByOrderByDownloadCountDesc());
    }

    // API: Vinh danh tài liệu được xem nhiều nhất
    @GetMapping("/top-views")
    public ResponseEntity<?> getTopViews() {
        return ResponseEntity.ok(studyMaterialRepository.findTop10ByOrderByViewCountDesc());
    }
}