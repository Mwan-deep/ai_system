package AI_Study_Hub.service;

import AI_Study_Hub.entity.*;
import AI_Study_Hub.repository.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizGeneratorService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final MaterialContextRepository contextRepository;
    private final AccountRepository accountRepository;
    private final StudyMaterialRepository materialRepository;
    private final GeminiService geminiService;

    @Transactional
    public Quiz generateQuizFromMaterial(Long materialId, Long accountId, int quantity) {
        // 1. Kiểm tra tài khoản và ngữ cảnh tài liệu học tập
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        StudyMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
        MaterialContext context = contextRepository.findByStudyMaterial_MaterialId(materialId)
                .orElseThrow(() -> new RuntimeException("Tài liệu chưa được phân tích nội dung văn bản để sinh trắc nghiệm"));

        // 2. Thiết kế Prompt ép AI trả về định dạng mảng JSON thuần túy
        String prompt = "Dựa trên nội dung tài liệu học tập được cung cấp dưới đây, hãy tạo ra đúng " + quantity + " câu hỏi trắc nghiệm ôn tập. " +
                "Mỗi câu hỏi phải có từ 4 phương án lựa chọn, và chỉ có duy nhất một phương án đúng.\n\n" +
                "YÊU CẦU BẮT BUỘC: Bạn chỉ được phép trả về chuỗi định dạng JSON thuần túy theo cấu trúc mẫu sau, không được chứa bất kỳ từ giải thích nào ngoài khối JSON:\n" +
                "[\n" +
                "  {\n" +
                "    \"questionText\": \"Nội dung câu hỏi trắc nghiệm?\",\n" +
                "    \"options\": [\n" +
                "      {\"text\": \"Phương án A\", \"isCorrect\": true},\n" +
                "      {\"text\": \"Phương án B\", \"isCorrect\": false},\n" +
                "      {\"text\": \"Phương án C\", \"isCorrect\": false},\n" +
                "      {\"text\": \"Phương án D\", \"isCorrect\": false}\n" +
                "    ]\n" +
                "  }\n" +
                "]\n\n" +
                "--- NỘI DUNG TÀI LIỆU ---\n" + context.getExtractedText();

        String aiResponse = geminiService.chatWithGemini(prompt).trim();

        // Xử lý loại bỏ thẻ định dạng markdown nếu AI tự ý bọc khối JSON (ví dụ: ```json ... ```)
        if (aiResponse.startsWith("```json")) {
            aiResponse = aiResponse.substring(7);
        }
        if (aiResponse.endsWith("```")) {
            aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
        }
        aiResponse = aiResponse.trim();

        // 3. Khởi tạo thực thể Quiz cha
        Quiz quiz = Quiz.builder()
                .account(account)
                .title("Bài trắc nghiệm tự động: " + material.getTitle())
                .quantity(quantity)
                .duration(LocalTime.of(0, 15)) // Mặc định thời gian làm bài 15 phút
                .passScore(5.0)
                .questions(new ArrayList<>())
                .build();
        quiz = quizRepository.save(quiz);

        // 4. Phân tích chuỗi JSON trả về từ AI bằng Gson để lưu dữ liệu con
        try {
            JsonArray questionsArray = JsonParser.parseString(aiResponse).getAsJsonArray();
            List<Question> savedQuestions = new ArrayList<>();

            for (JsonElement qElem : questionsArray) {
                JsonObject qObj = qElem.getAsJsonObject();
                String qText = qObj.get("questionText").getAsString();

                // Tạo câu hỏi (ĐÃ SỬA LẠI LIÊN KẾT)
                Question question = Question.builder()
                        .materialContext(context) // <--- Sửa dòng này, trỏ vào context thay vì material
                        .questionText(qText)
                        .build();
                question = questionRepository.save(question);

                JsonArray optionsArray = qObj.getAsJsonArray("options");
                String correctText = "";

                // Duyệt danh sách các phương án của câu hỏi
                for (JsonElement oElem : optionsArray) {
                    JsonObject oObj = oElem.getAsJsonObject();
                    String oText = oObj.get("text").getAsString();
                    boolean isCorrect = oObj.get("isCorrect").getAsBoolean();

                    QuestionOption option = QuestionOption.builder()
                            .question(question)
                            .optionText(oText)
                            .isCorrect(isCorrect)
                            .build();
                    optionRepository.save(option);

                    if (isCorrect) {
                        correctText = oText;
                    }
                }

                // Cập nhật lại đáp án đúng dạng văn bản cho Question
                question.setCorrectAnswer(correctText);
                questionRepository.save(question);

                savedQuestions.add(question);
            }

            // Gắn danh sách câu hỏi vào Quiz qua bảng trung gian quiz_questions
            quiz.setQuestions(savedQuestions);
            return quizRepository.save(quiz);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi phân tích cú pháp dữ liệu câu hỏi từ AI: " + e.getMessage() + ". Nội dung thô: " + aiResponse);
        }
    }
}