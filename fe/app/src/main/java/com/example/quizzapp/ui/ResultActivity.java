package com.example.quizzapp.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizzapp.R;
import com.example.quizzapp.models.AnswerOption;
import com.example.quizzapp.models.QuestionResponse;

import java.util.List;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        LinearLayout lnResultContainer = findViewById(R.id.lnResultContainer);
        Button btnBackHome = findViewById(R.id.btnBackHome);

        // Nhận dữ liệu thông qua GetExtras Bundle
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Map<Integer, String> userAnswers = (Map<Integer, String>) bundle.getSerializable("USER_ANSWERS");
            List<QuestionResponse> questionList = (List<QuestionResponse>) bundle.getSerializable("QUESTION_LIST");

            if (questionList != null && userAnswers != null) {
                for (int i = 0; i < questionList.size(); i++) {
                    QuestionResponse q = questionList.get(i);
                    String answered = userAnswers.get(i); // Nội dung chữ đáp án user đã chọn

                    // 1. Tạo một Layout dọc bọc riêng cho từng câu hỏi
                    LinearLayout lnQuestionBlock = new LinearLayout(this);
                    lnQuestionBlock.setOrientation(LinearLayout.VERTICAL);
                    lnQuestionBlock.setPadding(0, 16, 0, 32); // Khoảng cách đệm giữa các câu

                    // 2. Hiển thị tiêu đề Câu hỏi (In đậm)
                    TextView tvQuestionTitle = new TextView(this);
                    tvQuestionTitle.setText("Câu " + (i + 1) + ": " + q.getQuestionText());
                    tvQuestionTitle.setTextSize(16);
                    tvQuestionTitle.setTypeface(null, Typeface.BOLD);
                    tvQuestionTitle.setTextColor(Color.parseColor("#1E293B"));
                    lnQuestionBlock.addView(tvQuestionTitle);

                    // 3. Duyệt qua danh sách các lựa chọn để hiển thị trạng thái màu sắc
                    List<AnswerOption> options = q.getOptions();
                    if (options != null) {
                        for (AnswerOption option : options) {
                            TextView tvOptionItem = new TextView(this);
                            tvOptionItem.setTextSize(15);
                            tvOptionItem.setPadding(24, 8, 0, 8); // Đẩy lùi đáp án vào một chút so với tiêu đề

                            // Kiểm tra trạng thái đáp án
                            boolean isUserSelectedThis = option.getAnswerText().equals(answered);
                            boolean isCorrectAnswer = option.isCorrect();

                            if (isUserSelectedThis) {
                                if (isCorrectAnswer) {
                                    // Trường hợp 1: User chọn ĐÚNG -> Màu XANH LÁ CÂY + Icon tích
                                    tvOptionItem.setText("✓ " + option.getAnswerText());
                                    tvOptionItem.setTextColor(Color.parseColor("#10B981"));
                                    tvOptionItem.setTypeface(null, Typeface.BOLD);
                                } else {
                                    // Trường hợp 2: User chọn SAI -> Màu ĐỎ + Icon gạch chéo
                                    tvOptionItem.setText("✗ " + option.getAnswerText());
                                    tvOptionItem.setTextColor(Color.parseColor("#EF4444"));
                                    tvOptionItem.setTypeface(null, Typeface.BOLD);
                                }
                            } else if (isCorrectAnswer) {
                                // Trường hợp 3: Đây là đáp án ĐÚNG thực tế nhưng user bỏ lỡ -> Sáng màu XANH LÁ CÂY lên để gợi ý
                                tvOptionItem.setText("✓ " + option.getAnswerText());
                                tvOptionItem.setTextColor(Color.parseColor("#10B981"));
                                tvOptionItem.setTypeface(null, Typeface.BOLD);

                            } else {
                                // Trường hợp 4: Các đáp án sai thông thường mà user không chọn -> Màu xám nhạt mặc định
                                tvOptionItem.setText(option.getAnswerText());
                                tvOptionItem.setTextColor(Color.parseColor("#64748B"));
                                tvOptionItem.setTypeface(null, Typeface.BOLD);

                            }

                            // Thêm dòng đáp án vào cụm câu hỏi hiện tại
                            lnQuestionBlock.addView(tvOptionItem);
                        }
                    }

                    // Đổ toàn bộ cụm câu hỏi + 4 đáp án đã nhuộm màu vào Layout chính của màn hình
                    lnResultContainer.addView(lnQuestionBlock);
                }
            }
        }

        // Sự kiện nút quay lại trang chủ công khai
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}