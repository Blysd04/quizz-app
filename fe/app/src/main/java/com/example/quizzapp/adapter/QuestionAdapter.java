package com.example.quizzapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quizzapp.R;
import com.example.quizzapp.models.QuestionResponse;
import com.example.quizzapp.ui.AddEditQuestionActivity;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private List<QuestionResponse> questionList;
    private Context context;
    private String userToken;

    public QuestionAdapter(List<QuestionResponse> questionList, Context context) {
        this.questionList = questionList;
        this.context = context;
        this.userToken = userToken;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionResponse question = questionList.get(position);
        holder.tvCategory.setText(question.getCategory().toUpperCase());
        holder.tvQuestionText.setText(question.getQuestionText());

        // Xử lý sự kiện bấm nút Edit trên từng câu hỏi ở trang chủ
        holder.btnEditShortcut.setOnClickListener(v -> {
            // Lưu ý: 'context' thường là view.getContext()
            Intent intent = new Intent(context, AddEditQuestionActivity.class);
            intent.putExtra("IS_EDIT_MODE", true); // Chế độ SỬA
            intent.putExtra("QUESTION_ID", question.getId());
            intent.putExtra("USER_TOKEN", userToken);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return questionList != null ? questionList.size() : 0;
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvQuestionText, btnEditShortcut;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            btnEditShortcut = itemView.findViewById(R.id.btnEditShortcut);
        }
    }
}