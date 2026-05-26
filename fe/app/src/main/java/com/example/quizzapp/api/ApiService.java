package com.example.quizzapp.api;

import com.example.quizzapp.models.QuestionRequest;
import com.example.quizzapp.models.QuestionResponse;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    // 1. Lấy tất cả câu hỏi ở trang chủ
    @GET("api/questions")
    Call<List<QuestionResponse>> getAllQuestions();

    // 2. Lấy câu hỏi của chính tôi (Cần token)
    @GET("api/questions/my-questions")
    Call<List<QuestionResponse>> getMyQuestions(@Header("Authorization") String token);

    @GET("api/questions/{id}")
    Call<QuestionResponse> getQuestionById(@Header("Authorization") String token, @Path("id") String questionId);

    // 3. Tạo câu hỏi mới
    @POST("api/questions")
    Call<QuestionResponse> createQuestion(
            @Header("Authorization") String token,
            @Body QuestionRequest request // Truyền trực tiếp Object vào đây
    );

    @PUT("api/questions/{id}")
    Call<QuestionResponse> updateQuestion(@Header("Authorization") String token, @Path("id") String id, @Body QuestionRequest request);

    // 5. Delete câu hỏi (Xóa)
    @DELETE("api/questions/{id}")
    Call<Void> deleteQuestion(
            @Header("Authorization") String token,
            @Path("id") String questionId
    );

    @POST("api/questions/me/fcm-token")
    Call<Void> updateFcmToken(@Header("Authorization") String token, @Body Map<String, String> body);
}