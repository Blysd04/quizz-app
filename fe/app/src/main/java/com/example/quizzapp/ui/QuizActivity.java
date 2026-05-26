package com.example.quizzapp.ui; // Nhớ đổi đúng package ui của bạn

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizzapp.R;
import com.example.quizzapp.api.ApiClient; // Import ApiClient của bạn
import com.example.quizzapp.api.ApiService; // Import ApiService của bạn
import com.example.quizzapp.models.AnswerOption; // Import model option nếu có
import com.example.quizzapp.models.QuestionResponse; // SỬ DỤNG MODEL NÀY CỦA BẠN

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizActivity extends AppCompatActivity {

    private TextView tvProgress, tvQuestionText;
    private LinearLayout lnAnswersContainer;
    private Button btnNext;

    // Đã đổi hoàn toàn từ Question sang QuestionResponse khớp với project của bạn
    private List<QuestionResponse> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;

    // Lưu trữ đáp án user chọn: Key là vị trí câu hỏi, Value là nội dung đáp án chọn
    private Map<Integer, String> userAnswers = new HashMap<>();
    private String selectedAnswer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ĐÃ SỬA LỖI ANNOTATION TẠI ĐÂY
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_test);

        // Ánh xạ View từ layout
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        lnAnswersContainer = findViewById(R.id.lnAnswersContainer);
        btnNext = findViewById(R.id.btnNext);

        // Gọi hàm kết nối API để lấy câu hỏi thật từ database
        getQuestionsFromAPI();

        // Xử lý sự kiện bấm nút TIẾP THEO / HOÀN THÀNH
        btnNext.setOnClickListener(v -> {
            if (selectedAnswer.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn một đáp án trước khi tiếp tục!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu đáp án đã chọn vào Map kết quả
            userAnswers.put(currentQuestionIndex, selectedAnswer);

            // Kiểm tra xem còn câu hỏi tiếp theo không
            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                selectedAnswer = ""; // Reset đáp án lựa chọn cho câu tiếp theo
                displayQuestion();
            } else {
                // Nếu là câu cuối cùng -> Gói dữ liệu chuyển sang trang kết quả
                Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
                intent.putExtra("USER_ANSWERS", (Serializable) userAnswers);
                intent.putExtra("QUESTION_LIST", (Serializable) questionList);
                startActivity(intent);
//                finish();
            }
        });
    }

    // Hàm gọi API lấy danh sách câu hỏi thực tế từ MongoDB đổ lên
    private void getQuestionsFromAPI() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Lấy UID của user hiện tại đang đăng nhập từ Firebase Auth
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        apiService.getAllQuestions().enqueue(new Callback<List<QuestionResponse>>() {
            @Override
            public void onResponse(Call<List<QuestionResponse>> call, Response<List<QuestionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    questionList.clear();

                    // VÒNG LẶP LỌC: Chỉ add những câu hỏi nào có userId trùng với người dùng hiện tại
                    for (QuestionResponse q : response.body()) {
                        // kiểm tra kĩ hàm getId() hoặc getUserId() hoặc getCreatorId() trong QuestionResponse
                        if (q.getUserId() != null && q.getUserId().equals(currentUserId)) {
                            questionList.add(q);
                        }
                    }

                    // RANDOM CÂU HỎI: Xáo trộn toàn bộ danh sách câu hỏi của user này
                    Collections.shuffle(questionList);

                    if (!questionList.isEmpty()) {
                        displayQuestion();
                    } else {
                        tvQuestionText.setText("Bạn chưa tạo câu hỏi nào để làm bài test!");
                    }
                } else {
                    Toast.makeText(QuizActivity.this, "Không thể tải danh sách câu hỏi!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<QuestionResponse>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi kết nối mạng: " + t.getMessage());
                Toast.makeText(QuizActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 2. HÀM HIỂN THỊ CÂU HỎI LÊN GIAO DIỆN (ĐÃ THÊM RANDOM ĐÁP ÁN)
    private void displayQuestion() {
        // Xóa sạch các nút đáp án cũ của câu trước
        lnAnswersContainer.removeAllViews();

        QuestionResponse currentQuestion = questionList.get(currentQuestionIndex);

        // Cập nhật số lượng tổng các câu và câu hiện tại đang làm (Dạng: 01/03)
        String progressText = String.format("%02d/%02d", (currentQuestionIndex + 1), questionList.size());
        tvProgress.setText(progressText);

        // Hiển thị nội dung câu hỏi
        tvQuestionText.setText(currentQuestion.getQuestionText());

        // Lấy danh sách đáp án từ model
        List<AnswerOption> options = currentQuestion.getOptions();

        if (options != null) {
            // RANDOM ĐÁP ÁN: Tạo một list mới để xáo trộn, tránh làm hỏng cấu trúc dữ liệu gốc
            List<AnswerOption> shuffledOptions = new ArrayList<>(options);
            Collections.shuffle(shuffledOptions);

            for (AnswerOption option : shuffledOptions) {
                Button btnOption = new Button(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 24);
                btnOption.setLayoutParams(params);

                // Lấy text hiển thị đáp án bằng hàm
                String optionText = option.getAnswerText();
                btnOption.setText(optionText);

                btnOption.setTextSize(16);
                btnOption.setAllCaps(false);

                // Giao diện nút mặc định (Chưa chọn)
                btnOption.setBackgroundColor(Color.parseColor("#FFFFFF"));
                btnOption.setTextColor(Color.parseColor("#1E293B"));

                // Sự kiện click chọn đáp án
                btnOption.setOnClickListener(v -> {
                    // Reset toàn bộ các nút đáp án khác quay về màu trắng nền
                    for (int i = 0; i < lnAnswersContainer.getChildCount(); i++) {
                        View child = lnAnswersContainer.getChildAt(i);
                        if (child instanceof Button) {
                            child.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            ((Button) child).setTextColor(Color.parseColor("#1E293B"));
                        }
                    }
                    // Đổi màu nút vừa được người dùng bấm chọn sang màu tím của app
                    btnOption.setBackgroundColor(Color.parseColor("#E5D5FF"));
                    btnOption.setTextColor(Color.parseColor("#7F3DFF"));

                    // Lưu đáp án dạng chữ vào biến tạm
                    selectedAnswer = optionText;
                });

                lnAnswersContainer.addView(btnOption);
            }
        }

        // Đổi chữ hiển thị của nút điều hướng ở câu cuối cùng
        if (currentQuestionIndex == questionList.size() - 1) {
            btnNext.setText("HOÀN THÀNH");
        } else {
            btnNext.setText("TIẾP THEO");
        }
    }
}