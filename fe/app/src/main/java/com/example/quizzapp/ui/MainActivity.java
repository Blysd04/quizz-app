package com.example.quizzapp.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quizzapp.adapter.QuestionAdapter;
import com.example.quizzapp.databinding.ActivityMainBinding;
import com.example.quizzapp.models.QuestionResponse;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private QuestionAdapter adapter;
    private List<QuestionResponse> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Cấu hình RecyclerView hiển thị danh sách câu hỏi
        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionAdapter(list, this);
        binding.rvQuestions.setAdapter(adapter);

        // Gọi hàm lấy dữ liệu từ API ở đây để gán vào 'list' và gọi adapter.notifyDataSetChanged()

        // Bắt sự kiện nhấn nút FAB Tròn màu tím để chuyển sang form Thêm mới
        binding.fabAddQuestion.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditQuestionActivity.class);
            intent.putExtra("IS_EDIT_MODE", false); // Chế độ Thêm mới
            startActivity(intent);
        });
    }
}