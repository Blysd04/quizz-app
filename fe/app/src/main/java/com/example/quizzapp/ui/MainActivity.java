package com.example.quizzapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quizzapp.R;
import com.example.quizzapp.adapter.QuestionAdapter;
import com.example.quizzapp.api.ApiClient;
import com.example.quizzapp.api.ApiService;
import com.example.quizzapp.databinding.ActivityMainBinding;
import com.example.quizzapp.models.QuestionResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private QuestionAdapter adapter;
    private final List<QuestionResponse> questionList = new ArrayList<>();
    private String userToken;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ưu tiên lấy token từ SharedPreferences (đã lưu ở LoginActivity)
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userToken = prefs.getString("auth_token", null);

        setupRecyclerView();

        binding.fabAddQuestion.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditQuestionActivity.class);
            intent.putExtra("IS_EDIT_MODE", false);
            startActivity(intent);
        });

        binding.btnLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            prefs.edit().remove("auth_token").apply(); // Xóa token khi logout
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        Button btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionAdapter(questionList, this);
        binding.rvQuestions.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Luôn làm mới dữ liệu khi quay lại màn hình
        if (userToken != null) {
            getQuestionsList();
        } else {
            Log.e(TAG, "Token trống, không thể tải danh sách!");
        }
    }

    private void getQuestionsList() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String authHeader = userToken.startsWith("Bearer ") ? userToken : "Bearer " + userToken;

        apiService.getMyQuestions(authHeader).enqueue(new Callback<List<QuestionResponse>>() {
            @Override
            public void onResponse(Call<List<QuestionResponse>> call, Response<List<QuestionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    questionList.clear();
                    questionList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Đã tải thành công " + questionList.size() + " câu hỏi.");
                } else {
                    Log.e(TAG, "Lỗi API: " + response.code());
                    if (response.code() == 401) {
                        Toast.makeText(MainActivity.this, "Phiên đăng nhập hết hạn!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<QuestionResponse>> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Không thể kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}