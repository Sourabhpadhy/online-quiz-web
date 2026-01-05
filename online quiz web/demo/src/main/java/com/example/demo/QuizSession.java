package com.example.demo;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@SessionScope
public class QuizSession {
    public static final String STATUS_NOT_VISITED = "NOT_VISITED";
    public static final String STATUS_NOT_ANSWERED = "NOT_ANSWERED";
    public static final String STATUS_ANSWERED = "ANSWERED";
    public static final String STATUS_MARKED = "MARKED_FOR_REVIEW";
    public static final String STATUS_ANSWERED_MARKED = "ANSWERED_MARKED_FOR_REVIEW";

    private String studentName;
    private String enrollmentNo;  // New
    private String studentClass;  // New
    private String department;    // New
    
    private List<Question> questions = new ArrayList<>();
    private Map<Integer, String> userAnswers = new HashMap<>();
    private Map<Integer, String> questionStatus = new HashMap<>();
    private int currentIndex = 0;
    private LocalDateTime startTime;
    private LocalDateTime endTime; // New

    public void startNewQuiz(String name, String enrollmentNo, String sClass, String dept, List<Question> questions) {
        this.studentName = name;
        this.enrollmentNo = enrollmentNo;
        this.studentClass = sClass;
        this.department = dept;
        this.questions = questions;
        this.userAnswers.clear();
        this.questionStatus.clear();
        this.currentIndex = 0;
        this.startTime = LocalDateTime.now();
        this.endTime = null;
        
        for(int i=0; i<questions.size(); i++) {
            questionStatus.put(i, STATUS_NOT_VISITED);
        }
        questionStatus.put(0, STATUS_NOT_ANSWERED);
    }

    public void endQuiz() {
        if (this.endTime == null) {
            this.endTime = LocalDateTime.now();
        }
    }

    public String getTimeTaken() {
        if (startTime == null) return "00:00";
        LocalDateTime end = (endTime != null) ? endTime : LocalDateTime.now();
        Duration duration = Duration.between(startTime, end);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%02d min %02d sec", minutes, seconds);
    }

    // Standard getters and setters
    public void updateStatus(int index, String status) { questionStatus.put(index, status); }
    public void saveAnswer(int index, String answer) { userAnswers.put(index, answer); }
    public void clearResponse(int index) { userAnswers.remove(index); questionStatus.put(index, STATUS_NOT_ANSWERED); }

    public int calculateScore() {
        int score = 0;
        for (Map.Entry<Integer, String> entry : userAnswers.entrySet()) {
            int index = entry.getKey();
            String selected = entry.getValue();
            String correct = questions.get(index).getCorrectOption();
            if (selected != null && selected.equalsIgnoreCase(correct)) {
                score++;
            }
        }
        return score;
    }

    public String getStudentName() { return studentName; }
    public String getEnrollmentNo() { return enrollmentNo; }
    public String getStudentClass() { return studentClass; }
    public String getDepartment() { return department; }
    public List<Question> getQuestions() { return questions; }
    public Map<Integer, String> getUserAnswers() { return userAnswers; }
    public Map<Integer, String> getQuestionStatus() { return questionStatus; }
    public int getCurrentIndex() { return currentIndex; }
    public void setCurrentIndex(int index) { this.currentIndex = index; }
    public LocalDateTime getStartTime() { return startTime; }
    
    public Question getCurrentQuestion() {
        if (questions == null || currentIndex >= questions.size()) return null;
        return questions.get(currentIndex);
    }
}