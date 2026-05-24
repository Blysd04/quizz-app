package com.example.quizzapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.quizzapp.ui.AddEditQuestionActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM_SERVICE";
    private static final String CHANNEL_ID = "QUIZ_APP_FCM_CHANNEL";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // In FCM Token ra Logcat theo đúng tiêu chí đạt của bài Lab
        Log.d(TAG, "Refreshed FCM Token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 1. Khởi tạo nội dung hiển thị mặc định
        String title = "Quiz Master Update!";
        String message = "Có dữ liệu mới cần bạn kiểm tra.";

        // Nếu thông báo gửi về dạng thông thường (có Title/Body hiển thị sẵn)
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
        }

        // 2. XỬ LÝ ĐỌC CUSTOM DATA (DEEP LINK BẢO MẬT KÉP)
        Map<String, String> data = remoteMessage.getData();
        String questionId = null;

        if (data.size() > 0) {
            // Lấy biến type (có thể là "question" của bạn hoặc "product" của thầy)
            String type = data.get("type");

            if (type != null && type.equals("question") ) {

                questionId = data.get("questionId");

                // Bạn có thể tùy biến lại nội dung chữ hiển thị cho phù hợp với QuizApp
                if (remoteMessage.getNotification() == null) {
                    title = "Cập Nhật Câu Hỏi Mới!";
                    message = "Bấm vào để chỉnh sửa câu hỏi số: " + questionId;
                }
            }
        }

        // 3. Gửi toàn bộ dữ liệu sang hàm tạo Notification trên thanh trạng thái điện thoại
        sendNotification(title, message, questionId);
    }

    private void sendNotification(String title, String messageBody, String questionId) {
        // Cấu hình Intent Deep Link mở trực tiếp màn hình chỉnh sửa câu hỏi
        Intent intent = new Intent(this, AddEditQuestionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (questionId != null) {
            intent.putExtra("QUESTION_ID", questionId);
            intent.putExtra("IS_EDIT_MODE", true);
        }

        // Cờ cấu hình PendingIntent an toàn theo tiêu chuẩn Android mới
        int pendingFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_ONE_SHOT;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingFlags);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Đăng ký Channel cho các thiết bị Android từ 8.0 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Quiz App FCM Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}