package com.example.quizzapp.models;
import java.io.Serializable;

public class AnswerOption implements Serializable{
    private String answerText;
    private boolean isCorrect;

    public AnswerOption(String answerText, boolean isCorrect) {
        this.answerText = answerText;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public boolean isCorrect() { return isCorrect; }
    public void setIsCorrect(boolean isCorrect) { this.isCorrect = isCorrect; }
}