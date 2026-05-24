package com.example.quizzapp.services;

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

        String title = "Quiz Master Update!";
        String message = "Có dữ liệu mới cần bạn kiểm tra.";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
        }

        // Xử lý đọc Custom Data gửi kèm từ Node.js server hoặc Firebase Console
        Map<String, String> data = remoteMessage.getData();
        String questionId = data.get("questionId"); // Lấy ID câu hỏi để làm Deep Link

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