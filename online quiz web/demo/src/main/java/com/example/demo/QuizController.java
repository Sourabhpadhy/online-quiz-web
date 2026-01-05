package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class QuizController {

    private final AIService aiService;
    private final ProctoringService proctoringService;
    private final QuizSession quizSession;

    private static final int EXAM_DURATION_SEC = 600;

    public QuizController(AIService aiService, ProctoringService proctoringService, QuizSession quizSession) {
        this.aiService = aiService;
        this.proctoringService = proctoringService;
        this.quizSession = quizSession;
    }

    @GetMapping("/")
    public String showLogin() { return "login"; }

    @PostMapping("/start")
    public String startExam(
            @RequestParam String name, 
            @RequestParam String enrollment,
            @RequestParam String sClass,
            @RequestParam String department,
            @RequestParam String topic, 
            @RequestParam String difficulty) {
        
        List<Question> questions = aiService.fetchQuestionsFromAI(topic, difficulty);
        quizSession.startNewQuiz(name, enrollment, sClass, department, questions);
        proctoringService.startProctoring(name);
        return "redirect:/quiz";
    }

    @GetMapping("/quiz")
    public String showQuiz(Model model) {
        if (isTimeUp()) return "redirect:/result";

        int currentIdx = quizSession.getCurrentIndex();

        if (QuizSession.STATUS_NOT_VISITED.equals(quizSession.getQuestionStatus().get(currentIdx))) {
            quizSession.updateStatus(currentIdx, QuizSession.STATUS_NOT_ANSWERED);
        }

        model.addAttribute("q", quizSession.getCurrentQuestion());
        model.addAttribute("index", currentIdx);
        model.addAttribute("displayIndex", currentIdx + 1);
        model.addAttribute("total", quizSession.getQuestions().size());
        model.addAttribute("name", quizSession.getStudentName());
        model.addAttribute("enrollment", quizSession.getEnrollmentNo()); // Show in sidebar if needed
        model.addAttribute("questionStatus", quizSession.getQuestionStatus());
        model.addAttribute("userAnswers", quizSession.getUserAnswers());
        model.addAttribute("currentSelection", quizSession.getUserAnswers().get(currentIdx));

        long secondsElapsed = Duration.between(quizSession.getStartTime(), LocalDateTime.now()).getSeconds();
        model.addAttribute("timeLeft", Math.max(0, EXAM_DURATION_SEC - secondsElapsed));
        
        return "quiz";
    }

    @PostMapping("/submitAnswer")
    public String handleQuizAction(
            @RequestParam(required = false) String selectedOption, 
            @RequestParam String action) { 
        
        if (isTimeUp()) return "redirect:/result";

        int currentIndex = quizSession.getCurrentIndex();

        if ("clear".equals(action)) {
            quizSession.clearResponse(currentIndex);
            return "redirect:/quiz";
        }

        if (selectedOption != null && !selectedOption.isEmpty()) {
            quizSession.saveAnswer(currentIndex, selectedOption);
        }

        if ("saveNext".equals(action)) {
            if (selectedOption != null) {
                quizSession.updateStatus(currentIndex, QuizSession.STATUS_ANSWERED);
            } else {
                quizSession.updateStatus(currentIndex, QuizSession.STATUS_NOT_ANSWERED);
            }
            if (currentIndex < quizSession.getQuestions().size() - 1) {
                quizSession.setCurrentIndex(currentIndex + 1);
            }

        } else if ("markReview".equals(action)) {
             if (selectedOption != null) {
                 quizSession.updateStatus(currentIndex, QuizSession.STATUS_ANSWERED_MARKED);
             } else {
                 quizSession.updateStatus(currentIndex, QuizSession.STATUS_MARKED);
             }
            if (currentIndex < quizSession.getQuestions().size() - 1) {
                quizSession.setCurrentIndex(currentIndex + 1);
            }

        } else if ("submit".equals(action)) {
            return "redirect:/result";

        } else {
            try {
                int jumpIndex = Integer.parseInt(action);
                if (jumpIndex >= 0 && jumpIndex < quizSession.getQuestions().size()) {
                    String currentStatus = quizSession.getQuestionStatus().get(currentIndex);
                    if (QuizSession.STATUS_NOT_VISITED.equals(currentStatus)) {
                         quizSession.updateStatus(currentIndex, QuizSession.STATUS_NOT_ANSWERED);
                    }
                    quizSession.setCurrentIndex(jumpIndex);
                }
            } catch (NumberFormatException e) {
            }
        }

        return "redirect:/quiz";
    }

    @GetMapping("/result")
    public String showResult(Model model) {
        quizSession.endQuiz(); // Stop the timer
        model.addAttribute("score", quizSession.calculateScore());
        model.addAttribute("total", quizSession.getQuestions().size());
        model.addAttribute("name", quizSession.getStudentName());
        model.addAttribute("enrollment", quizSession.getEnrollmentNo());
        model.addAttribute("sClass", quizSession.getStudentClass());
        model.addAttribute("dept", quizSession.getDepartment());
        model.addAttribute("timeTaken", quizSession.getTimeTaken());
        return "result";
    }

    private boolean isTimeUp() {
        if (quizSession.getStartTime() == null) return true;
        long secondsElapsed = Duration.between(quizSession.getStartTime(), LocalDateTime.now()).getSeconds();
        return secondsElapsed > EXAM_DURATION_SEC;
    }
}