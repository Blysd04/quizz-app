package com.example.quizzapp.models;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

import java.util.List;

public class QuestionResponse implements Serializable{
    @SerializedName("_id")
    private String id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("questionText")
    private String questionText;

    @SerializedName("category")
    private String category;

    @SerializedName("options")
    private List<AnswerOption> options;

    // Getter
    public String getId() { return id; }
    public String getQuestionText() { return questionText; }
    public String getCategory() { return category; }
    public List<AnswerOption> getOptions() { return options; }
    public String getUserId() {return userId;}
}