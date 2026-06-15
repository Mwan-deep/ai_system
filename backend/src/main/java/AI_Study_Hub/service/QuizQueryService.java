package AI_Study_Hub.service;

import AI_Study_Hub.dto.QuizResponseDTO;
import AI_Study_Hub.entity.Question;
import AI_Study_Hub.entity.QuestionOption;
import AI_Study_Hub.entity.Quiz;
import AI_Study_Hub.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizQueryService {

    private final QuizRepository quizRepository;

    public QuizResponseDTO getQuizForTaking(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));

        // 1. Chuyển đổi danh sách Câu hỏi
        List<QuizResponseDTO.QuestionDTO> questionDTOs = new ArrayList<>();

        for (Question question : quiz.getQuestions()) {

            // 2. Chuyển đổi danh sách Phương án (Gọt bỏ cột isCorrect)
            List<QuizResponseDTO.OptionDTO> optionDTOs = new ArrayList<>();
            for (QuestionOption option : question.getQuestionOptions()) { // Cần đảm bảo Entity Question có map @OneToMany tới QuestionOption
                optionDTOs.add(QuizResponseDTO.OptionDTO.builder()
                        .optionId(option.getOptionId())
                        .optionText(option.getOptionText())
                        .build());
            }

            questionDTOs.add(QuizResponseDTO.QuestionDTO.builder()
                    .questionId(question.getQuestionId())
                    .questionText(question.getQuestionText())
                    .options(optionDTOs)
                    .build());
        }

        // 3. Đóng gói vào DTO cha
        return QuizResponseDTO.builder()
                .quizId(quiz.getQuizId())
                .title(quiz.getTitle())
                .quantity(quiz.getQuantity())
                .duration(quiz.getDuration() != null ? quiz.getDuration().getMinute() + " phút" : "Không giới hạn")
                .questions(questionDTOs)
                .build();
    }
}