package com.example.quizzapp.models;

import com.google.gson.annotations.SerializedName;

public class OptionRequest {
    @SerializedName("answerText")
    private String answerText;
    @SerializedName("isCorrect")
    private boolean isCorrect;

    public OptionRequest(String answerText, boolean isCorrect) {
        this.answerText = answerText;
        this.isCorrect = isCorrect;
    }
    // Getter và Setter
    public String getAnswerText() {
        return answerText;
    }
    public boolean isCorrect() { return isCorrect; }
}