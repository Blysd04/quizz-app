package com.example.quizzapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestionRequest {
    // 1. Đảm bảo các biến được khai báo rõ ràng
    private String userId;
    private String questionText;
    private String category;
    private List<OptionRequest> options;

    // 2. Constructor rỗng (BẮT BUỘC cho GSON)
    public QuestionRequest() {
    }

    // 3. Constructor đầy đủ tham số
    public QuestionRequest(String userId, String questionText, String category, List<OptionRequest> options) {
        this.userId = userId;
        this.questionText = questionText;
        this.category = category;
        this.options = options;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserId() {
        return userId;
    }
    public String getQuestionText() { return questionText; }
    public String getCategory() { return category; }
    public List<OptionRequest> getOptions() { return options; }

    // 5. Setter
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setCategory(String category) { this.category = category; }
    public void setOptions(List<OptionRequest> options) { this.options = options; }
}