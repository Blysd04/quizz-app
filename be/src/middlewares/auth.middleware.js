// import admin from '../config/firebase.js';

// export const verifyToken = async (req, res, next) => {
//   const authHeader = req.headers.authorization;

//   if (!authHeader || !authHeader.startsWith('Bearer ')) {
//     return res.status(401).json({ message: 'Thiếu hoặc sai định dạng Token (Bearer <token>)' });
//   }

//   const token = authHeader.split(' ')[1];

//   try {
//     // Giải mã token từ Firebase gửi lên
//     const decodedToken = await admin.auth().verifyIdToken(token);
//     // Gán thông tin user vào req để các hàm phía sau sử dụng
//     req.user = decodedToken; 
//     next();
//   } catch (error) {
//     return res.status(401).json({ message: 'Token không hợp lệ hoặc đã hết hạn', error: error.message });
//   }
// };

import admin from '../config/firebase.js';

export const verifyToken = async (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ message: 'Thiếu hoặc sai định dạng Token (Bearer <token>)' });
  }

  const token = authHeader.split(' ')[1];

  // ====== ĐOẠN CODE BYPASS ĐỂ TEST POSTMAN ======
  if (token === 'test-token-userA') {
    req.user = { uid: 'USER_A_FAKE_ID', email: 'usera@gmail.com' };
    return next();
  }
  if (token === 'test-token-userB') {
    req.user = { uid: 'USER_B_FAKE_ID', email: 'userb@gmail.com' };
    return next();
  }
  // ==============================================

  try {
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken; 
    next();
  } catch (error) {
    return res.status(401).json({ message: 'Token không hợp lệ hoặc đã hết hạn', error: error.message });
  }
};