package com.example.quizzapp.models;
import java.util.List;

public class QuestionResponse {
    private String _id;
    private String userId;
    private String questionText;
    private String category;
    private List<AnswerOption> options;
    private String createdAt;

    // Getters
    public String get_id() { return _id; }
    public String getUserId() { return userId; }
    public String getQuestionText() { return questionText; }
    public String getCategory() { return category; }
    public List<AnswerOption> getOptions() { return options; }
    public String getCreatedAt() { return createdAt; }
}