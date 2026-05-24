package com.example.quizzapp.api;

import com.example.quizzapp.models.QuestionResponse;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    // 1. Lấy tất cả câu hỏi ở trang chủ
    @GET("api/questions")
    Call<List<QuestionResponse>> getAllQuestions();

    // 2. Lấy câu hỏi của chính tôi (Cần token)
    @GET("api/questions/my-questions")
    Call<List<QuestionResponse>> getMyQuestions(@Header("Authorization") String token);

    // 3. Tạo câu hỏi mới
    @POST("api/questions")
    Call<QuestionResponse> createQuestion(
            @Header("Authorization") String token,
            @Body Map<String, Object> requestBody
    );
}