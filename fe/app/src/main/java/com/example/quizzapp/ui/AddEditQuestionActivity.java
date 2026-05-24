package com.example.quizapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quizzapp.R;
import com.example.quizzapp.databinding.ActivityAddEditQuestionBinding;
import java.util.ArrayList;

public class AddEditQuestionActivity extends AppCompatActivity {
    private ActivityAddEditQuestionBinding binding;
    private boolean isEditMode = false;
    private String questionId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditQuestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Kiểm tra xem intent truyền tới là Thêm hay Sửa (Kể cả click từ Home hay từ Noti FCM đổ về)
        if (getIntent() != null && getIntent().hasExtra("IS_EDIT_MODE")) {
            isEditMode = getIntent().getBooleanExtra("IS_EDIT_MODE", false);
            questionId = getIntent().getStringExtra("QUESTION_ID");
        }

        setupFormView();

        binding.tvAddOptionBtn.setOnClickListener(v -> addNewOptionRow("", false, true));
        binding.btnSave.setOnClickListener(v -> validateAndSaveData());
        binding.btnDelete.setOnClickListener(v -> executeDeleteQuestion());
    }

    private void setupFormView() {
        if (isEditMode) {
            binding.tvAdminTitle.setText("CHỈNH SỬA CÂU HỎI");
            binding.btnDelete.setVisibility(View.VISIBLE); // Chế độ sửa có nút xóa câu hỏi
            loadQuestionDataFromApi(questionId);
        } else {
            binding.tvAdminTitle.setText("THÊM CÂU HỎI MỚI");
            binding.btnDelete.setVisibility(View.GONE);    // Thêm mới ẩn nút xóa câu hỏi

            // Khởi tạo sẵn 2 ô trống mặc định
            addNewOptionRow("", false, false); // Ô 1: không cho phép xóa dòng (isRemovable = false)
            addNewOptionRow("", false, false); // Ô 2: không cho phép xóa dòng (isRemovable = false)
        }
    }

    private void addNewOptionRow(String text, boolean isCorrect, boolean isRemovable) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_admin_option_input, null);

        EditText etText = rowView.findViewById(R.id.etAnswerText);
        RadioButton rbCorrect = rowView.findViewById(R.id.rbIsCorrect);
        ImageButton btnRemove = rowView.findViewById(R.id.btnRemoveOption);

        etText.setText(text);
        rbCorrect.setChecked(isCorrect);

        // Đảm bảo chỉ chọn được duy nhất một đáp án đúng bằng RadioButton
        rbCorrect.setOnClickListener(v -> {
            int totalRows = binding.lnAdminOptionsContainer.getChildCount();
            for (int i = 0; i < totalRows; i++) {
                View child = binding.lnAdminOptionsContainer.getChildAt(i);
                RadioButton childRb = child.findViewById(R.id.rbIsCorrect);
                if (childRb != rbCorrect) {
                    childRb.setChecked(false);
                }
            }
        });

        // Thiết lập trạng thái hiển thị nút (x) xóa dòng đáp án
        if (isRemovable) {
            btnRemove.setOnClickListener(v -> {
                // Ràng buộc bảo vệ trực tiếp: không cho user xóa xuống ít hơn 2 đáp án
                if (binding.lnAdminOptionsContainer.getChildCount() <= 2) {
                    Toast.makeText(this, "Không thể xóa! Câu hỏi bắt buộc phải có ít nhất 2 đáp án.", Toast.LENGTH_SHORT).show();
                } else {
                    binding.lnAdminOptionsContainer.removeView(rowView);
                }
            });
        } else {
            btnRemove.setVisibility(View.INVISIBLE); // 2 ô mặc định lúc thêm mới sẽ ẩn nút x đi
        }

        binding.lnAdminOptionsContainer.addView(rowView);
    }

    private void validateAndSaveData() {
        int totalOptions = binding.lnAdminOptionsContainer.getChildCount();

        // KIỂM TRA ĐIỀU KIỆN 1: Ít hơn 2 đáp án
        if (totalOptions < 2) {
            Toast.makeText(this, "Lỗi kiểm tra dữ liệu: Phải có tối thiểu 2 đáp án trở lên!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasCheckedCorrect = false;

        for (int i = 0; i < totalOptions; i++) {
            View child = binding.lnAdminOptionsContainer.getChildAt(i);
            EditText etText = child.findViewById(R.id.etAnswerText);
            RadioButton rbCorrect = child.findViewById(R.id.rbIsCorrect);

            if (etText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng không để trống nội dung ô đáp án!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rbCorrect.isChecked()) {
                hasCheckedCorrect = true;
            }
        }

        // KIỂM TRA ĐIỀU KIỆN 2: Không có đáp án nào đúng
        if (!hasCheckedCorrect) {
            Toast.makeText(this, "Lỗi kiểm tra dữ liệu: Phải tích chọn 1 đáp án đúng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vượt qua kiểm tra, gọi API Retrofit gửi chuỗi lên Node.js lưu
        Toast.makeText(this, "Dữ liệu hợp lệ! Đang lưu lên cơ sở dữ liệu...", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadQuestionDataFromApi(String id) {
        // Giả lập lấy dữ liệu từ server về dựa theo ID câu hỏi
        binding.etQuestionText.setText("Nội dung câu hỏi cũ lấy từ Database");
        binding.etCategory.setText("Địa Lý");

        // Đổ mảng dữ liệu cũ lên form (Ở chế độ sửa thì các dòng đều có thể xóa được tự do)
        addNewOptionRow("Hà Nội", true, true);
        addNewOptionRow("Hải Phòng", false, true);
        addNewOptionRow("Đà Nẵng", false, true);
    }

    private void executeDeleteQuestion() {
        // Thực thi gọi API Delete xóa câu hỏi khỏi Database MongoDB
        Toast.makeText(this, "Đã xóa câu hỏi khỏi hệ thống!", Toast.LENGTH_SHORT).show();
        finish();
    }
}