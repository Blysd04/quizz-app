const admin = require("firebase-admin");

// 1. Điền đường dẫn tới file Key JSON chứa chứng chỉ bảo mật Firebase của bạn
// (File này lấy trên Firebase Console: Project Settings -> Service accounts -> Generate new private key)
const serviceAccount = require("../firebase-service-account.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// 2. DÁN CÁI FCM TOKEN BẠN LẤY ĐƯỢC TỪ LOGCAT ANDROID VÀO ĐÂY
const registrationToken = "DÁN_MÃ_FCM_TOKEN_DÀI_NGOẰNG_Ở_LOGCAT_CỦA_BẠN_VÀO_ĐÂY";

// 3. CẤU HÌNH PAYLOAD DATA-ONLY MESSAGE 
const message = {
  data: {
    type: "question",   
    questionId: "5"    
  },
  token: registrationToken
};

// 4. THỰC THI LỆNH GỬI THÔNG BÁO SANG ĐIỆN THOẠI
admin.messaging().send(message)
  .then((response) => {
    console.log(" Bắn thông báo thành công! Phản hồi từ Firebase:", response);
  })
  .catch((error) => {
    console.log(" Lỗi bắn thông báo thất bại:", error);
  });