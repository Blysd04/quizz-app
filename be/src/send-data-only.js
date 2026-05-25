import admin from "firebase-admin";
import { createRequire } from "module";

// Sử dụng createRequire để load file JSON an toàn
const require = createRequire(import.meta.url);
const serviceAccount = require("../firebase-service-account.json");

// Khởi tạo admin (nếu chưa khởi tạo ở file khác)
if (admin.apps.length === 0) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

// 1. CHUYỂN THÀNH HÀM LINH ĐỘNG NHẬN VÀO TOKEN VÀ QUESTION_ID
export const sendQuizNotification = async (registrationToken, questionId) => {
  const message = {
    data: {
      type: "question",   
      questionId: String(questionId)
    },
    token: registrationToken
  };

  try {
    const response = await admin.messaging().send(message);
    console.log(`Gửi thông báo thành công cho câu hỏi [${questionId}]!`, response);
  } catch (error) {
    console.log("Lỗi bắn thông báo thất bại:", error);
  }
};