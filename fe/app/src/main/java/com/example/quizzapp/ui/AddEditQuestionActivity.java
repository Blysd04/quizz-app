package com.example.quizzapp.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizzapp.R;
import com.example.quizzapp.api.ApiClient;
import com.example.quizzapp.api.ApiService;
import com.example.quizzapp.databinding.ActivityAddEditQuestionBinding;
import com.example.quizzapp.models.AnswerOption;
import com.example.quizzapp.models.OptionRequest;
import com.example.quizzapp.models.QuestionRequest;
import com.example.quizzapp.models.QuestionResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditQuestionActivity extends AppCompatActivity {
    private ActivityAddEditQuestionBinding binding;
    private boolean isEditMode = false;
    private String questionId = null;
    private String userToken = null;
    private final String TAG = "AddEditQuestionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditQuestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userToken = prefs.getString("auth_token", null);

        isEditMode = getIntent().getBooleanExtra("IS_EDIT_MODE", false);
        questionId = getIntent().getStringExtra("QUESTION_ID");

        setupFormView();

        binding.tvAddOptionBtn.setOnClickListener(v -> addNewOptionRow("", false, true));
        binding.btnSave.setOnClickListener(v -> validateAndSaveData());

        if (isEditMode) {
            binding.btnDelete.setOnClickListener(v -> executeDeleteQuestion());
        }
    }

    private void setupFormView() {
        if (isEditMode && questionId != null) {
            binding.tvAdminTitle.setText("CHỈNH SỬA CÂU HỎI");
            binding.btnDelete.setVisibility(View.VISIBLE);
            loadQuestionDataFromApi(questionId);
        } else {
            binding.tvAdminTitle.setText("THÊM CÂU HỎI MỚI");
            binding.btnDelete.setVisibility(View.GONE);
            addNewOptionRow("", false, false);
            addNewOptionRow("", false, false);
        }
    }

    private void addNewOptionRow(String text, boolean isCorrect, boolean isRemovable) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_admin_option_input, null);
        EditText etText = rowView.findViewById(R.id.etAnswerText);
        RadioButton rbCorrect = rowView.findViewById(R.id.rbIsCorrect);
        ImageButton btnRemove = rowView.findViewById(R.id.btnRemoveOption);

        etText.setText(text);
        rbCorrect.setChecked(isCorrect);

        rbCorrect.setOnClickListener(v -> {
            for (int i = 0; i < binding.lnAdminOptionsContainer.getChildCount(); i++) {
                View child = binding.lnAdminOptionsContainer.getChildAt(i);
                RadioButton rb = child.findViewById(R.id.rbIsCorrect);
                rb.setChecked(rb == rbCorrect);
            }
        });

        if (isRemovable) {
            btnRemove.setOnClickListener(v -> binding.lnAdminOptionsContainer.removeView(rowView));
        } else {
            btnRemove.setVisibility(View.INVISIBLE);
        }
        binding.lnAdminOptionsContainer.addView(rowView);
    }

    private void validateAndSaveData() {
        Log.d("DEBUG_ACTION", "Nút LƯU đã được nhấn");
        if (userToken == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OptionRequest> optionList = new ArrayList<>();
        boolean hasCorrect = false;

        for (int i = 0; i < binding.lnAdminOptionsContainer.getChildCount(); i++) {
            View child = binding.lnAdminOptionsContainer.getChildAt(i);
            EditText etText = child.findViewById(R.id.etAnswerText);
            RadioButton rbCorrect = child.findViewById(R.id.rbIsCorrect);

            String text = etText.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ đáp án!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rbCorrect.isChecked()) hasCorrect = true;
            optionList.add(new OptionRequest(text, rbCorrect.isChecked()));
        }

        if (!hasCorrect) {
            Toast.makeText(this, "Hãy chọn đáp án đúng!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        QuestionRequest request = new QuestionRequest(userId, binding.etQuestionText.getText().toString().trim(), binding.etCategory.getText().toString().trim(), optionList);

        if (isEditMode) updateQuestionToServer(request);
        else sendDataToServer(request);
    }

    private String getAuthHeader() {
        return userToken.startsWith("Bearer ") ? userToken : "Bearer " + userToken;
    }

    private void sendDataToServer(QuestionRequest request) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.createQuestion(getAuthHeader(), request).enqueue(new Callback<QuestionResponse>() {
            @Override
            public void onResponse(Call<QuestionResponse> call, Response<QuestionResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddEditQuestionActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Không có thông tin lỗi";
                        Log.e("DEBUG_API", "Lỗi Server (Code " + response.code() + "): " + errorBody);
                    } catch (Exception e) {
                        Log.e("DEBUG_API", "Lỗi đọc errorBody: " + e.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<QuestionResponse> call, Throwable t) { Log.e(TAG, "Lỗi mạng: " + t.getMessage()); }
        });
    }

    private void updateQuestionToServer(QuestionRequest request) {
        Log.d("DEBUG_ACTION", "Đang gọi API Update..."); // Log để biết nó đã chạy tới đây

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Đảm bảo authHeader có "Bearer "
        String authHeader = userToken.startsWith("Bearer ") ? userToken : "Bearer " + userToken;

        apiService.updateQuestion(authHeader, questionId, request).enqueue(new Callback<QuestionResponse>() {
            @Override
            public void onResponse(Call<QuestionResponse> call, Response<QuestionResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("DEBUG_API", "Cập nhật thành công!");
                    Toast.makeText(AddEditQuestionActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // ĐOẠN LOG ĐỂ BẮT LỖI SERVER
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                        Log.e("DEBUG_API", "Lỗi Update (Code " + response.code() + "): " + error);
                    } catch (Exception e) {
                        Log.e("DEBUG_API", "Lỗi đọc errorBody: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<QuestionResponse> call, Throwable t) {
                Log.e("DEBUG_API", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void loadQuestionDataFromApi(String id) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getQuestionById(getAuthHeader(), id).enqueue(new Callback<QuestionResponse>() {
            @Override
            public void onResponse(Call<QuestionResponse> call, Response<QuestionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    QuestionResponse data = response.body();
                    binding.etQuestionText.setText(data.getQuestionText());
                    binding.etCategory.setText(data.getCategory());
                    binding.lnAdminOptionsContainer.removeAllViews();
                    if (data.getOptions() != null) {
                        for (int i = 0; i < data.getOptions().size(); i++) {
                            AnswerOption option = data.getOptions().get(i);

                            // Nếu là 2 dòng đầu (index 0 và 1), thì set isRemovable = false
                            boolean canRemove = (i >= 2);

                            addNewOptionRow(option.getAnswerText(), option.isCorrect(), canRemove);
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<QuestionResponse> call, Throwable t) { Log.e(TAG, "Lỗi load: " + t.getMessage()); }
        });
    }

    private void executeDeleteQuestion() {
        if (questionId == null || questionId.isEmpty()) {
            Toast.makeText(this, "ID câu hỏi không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String authHeader = getAuthHeader(); // Sử dụng lại hàm getAuthHeader có sẵn

        apiService.deleteQuestion(authHeader, questionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("DEBUG_API", "Xóa thành công!");
                    Toast.makeText(AddEditQuestionActivity.this, "Đã xóa câu hỏi!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại màn hình trước đó sau khi xóa
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                        Log.e("DEBUG_API", "Lỗi Xóa (Code " + response.code() + "): " + error);
                        Toast.makeText(AddEditQuestionActivity.this, "Lỗi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("DEBUG_API", "Lỗi đọc errorBody: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DEBUG_API", "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(AddEditQuestionActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}