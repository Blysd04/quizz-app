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

import com.example.quizzapp.R;
import com.example.quizzapp.ui.AddEditQuestionActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM_SERVICE";
    private static final String CHANNEL_ID = "QUIZ_APP_FCM_CHANNEL";
    private static final String CHANNEL_NAME = "Quiz App Notifications";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Ghi log token để bạn copy gửi lên server trong lúc test
        Log.d(TAG, "Refreshed FCM Token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "Quiz Master";
        String message = "Bạn có thông báo mới từ hệ thống.";
        String questionId = null;

        // 1. Xử lý Notification Payload (nếu server gửi có title/body)
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
        }

        // 2. Xử lý Data Payload (để lấy questionId cho Deep Link)
        Map<String, String> data = remoteMessage.getData();
        if (!data.isEmpty()) {
            if ("question".equals(data.get("type"))) {
                questionId = data.get("questionId");
                // Nếu server không gửi notification payload, chúng ta tự tạo tiêu đề
                if (remoteMessage.getNotification() == null) {
                    title = "Cập Nhật Câu Hỏi!";
                    message = "Chạm để xem chi tiết câu hỏi.";
                }
            }
        }

        sendNotification(title, message, questionId);
    }

    private void sendNotification(String title, String messageBody, String questionId) {
        // Cấu hình Intent để mở Activity
        Intent intent = new Intent(this, AddEditQuestionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (questionId != null) {
            intent.putExtra("QUESTION_ID", questionId);
            intent.putExtra("IS_EDIT_MODE", true);
        }

        // Cấu hình PendingIntent chuẩn Android 12+ (Immutable là bắt buộc)
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        // Tạo Builder
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher) // Dùng icon app của bạn
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Đăng ký Channel cho Android O trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            // Dùng ID là 0 sẽ ghi đè thông báo cũ, dùng System.currentTimeMillis() nếu muốn hiển thị tất cả
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
    }
}