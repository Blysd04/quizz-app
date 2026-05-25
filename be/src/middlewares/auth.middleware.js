import admin from '../config/firebase.js';
import { db } from '../index.js';
import mongoose from 'mongoose';

export const verifyToken = async (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ message: 'Thiếu hoặc sai định dạng Token' });
  }

  const token = authHeader.split(' ')[1];

  try {
    // 1. Xác thực ID Token từ Firebase
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken; // Gán thông tin user (gồm uid)


    next();
  } catch (error) {
    return res.status(401).json({ message: 'Token không hợp lệ', error: error.message });
  }
};

export const sendWelcomeNotification = async (uid) => {
  try {
    
    // Tìm duy nhất 1 câu hỏi bất kỳ của user này để lấy fcmToken ra
    const questionRecord = await mongoose.connection.collection('questions').findOne({ userId: uid });
    
    if (!questionRecord) {
        console.log("==> User này chưa từng tạo câu hỏi nào trong hệ thống, không có dữ liệu để lấy token!");
        return;
    }
    
    
    if (questionRecord.fcmToken) {
      const message = {
        notification: {
          title: 'Chào mừng bạn!',
          body: 'Chúc bạn có những giờ phút học tập vui vẻ tại QuizzApp'
        },
        token: questionRecord.fcmToken
      };

      await admin.messaging().send(message);
    }
  } catch (error) {
    console.error("Lỗi gửi thông báo:", error);
  }
};


// import admin from '../config/firebase.js';

// export const verifyToken = async (req, res, next) => {
//   const authHeader = req.headers.authorization;

//   if (!authHeader || !authHeader.startsWith('Bearer ')) {
//     return res.status(401).json({ message: 'Thiếu hoặc sai định dạng Token (Bearer <token>)' });
//   }

//   const token = authHeader.split(' ')[1];

//   // ====== ĐOẠN CODE BYPASS ĐỂ TEST POSTMAN ======
//   if (token === 'test-token-userA') {
//     req.user = { uid: 'USER_A_FAKE_ID', email: 'usera@gmail.com' };
//     return next();
//   }
//   if (token === 'test-token-userB') {
//     req.user = { uid: 'USER_B_FAKE_ID', email: 'userb@gmail.com' };
//     return next();
//   }
//   // ==============================================

//   try {
//     const decodedToken = await admin.auth().verifyIdToken(token);
//     req.user = decodedToken; 
//     next();
//   } catch (error) {
//     return res.status(401).json({ message: 'Token không hợp lệ hoặc đã hết hạn', error: error.message });
//   }
// };