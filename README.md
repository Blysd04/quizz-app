# 📝 QuizzApp - Ứng dụng Thi Trắc Nghiệm Thông Minh

QuizzApp là một hệ thống ứng dụng thi trắc nghiệm hoàn chỉnh bao gồm ứng dụng di động (Android) và hệ thống máy chủ quản lý dữ liệu (Backend). Ứng dụng cho phép người dùng tham gia các bài kiểm tra kiến thức với cơ chế chống gian lận thông minh, tự động tải bộ câu hỏi cá nhân hóa theo từng tài khoản và chấm điểm, hiển thị kết quả trực quan ngay sau khi hoàn thành.

---

## ✨ Tính năng nổi bật

- **Xác thực người dùng bảo mật:** Đăng nhập, quản lý phiên làm việc thông qua Firebase Authentication kết hợp verify Token ở phía Backend.
- **Cá nhân hóa bộ đề:** Hệ thống tự động lọc dữ liệu thời gian thực từ MongoDB, chỉ hiển thị danh sách câu hỏi do chính tài khoản của User hiện tại sở hữu.
- **Cơ chế Đảo Đề & Đáp Án (Anti-Cheat):**
  - Xáo trộn ngẫu nhiên thứ tự xuất hiện của toàn bộ danh sách câu hỏi (`Collections.shuffle`).
  - Xáo trộn ngẫu nhiên vị trí các lựa chọn đáp án (A, B, C, D) độc lập bên trong mỗi câu hỏi để tránh việc học vẹt vị trí.
- **Báo cáo kết quả trực quan:** Màn hình kết quả hiển thị chi tiết toàn bộ bài thi:
  - Nhuộm **Màu Xanh Lá (Green)** cho phương án chính xác hoặc phương án đúng mà hệ thống gợi ý.
  - Nhuộm **Màu Đỏ (Red)** cho phương án người dùng chọn bị sai kèm ký tự nhận diện trực quan.

---

## 📂 Cấu trúc Thư mục Dự án

Dự án được phân chia rõ ràng thành 2 phần độc lập: `fe` (Frontend Android) và `be` (Backend Node.js).

```text
QuizzApp/
├── be/                          # BACKEND PROJECT (Node.js & Express)
│   ├── src/
│   │   ├── config/
│   │   │   └── firebase.js      # Cấu hình kết nối Firebase Admin SDK
│   │   ├── middlewares/
│   │   │   └── auth.middleware.js # Middleware xác thực Firebase Token gửi lên từ App
│   │   ├── models/
│   │   │   └── question.model.js # Schema định nghĩa cấu trúc Câu hỏi & Đáp án trong MongoDB
│   │   ├── routes/
│   │   │   └── question.route.js # Định nghĩa API Endpoints xử lý câu hỏi (GET, POST...)
│   │   └── index.js             # File chạy chính của Server Express
│   ├── .env                     # Lưu trữ biến môi trường bảo mật (PORT, MONGO_URI...)
│   └── firebase-service-account.json # Chứng chỉ bảo mật của Firebase Admin
│
├── fe/                          # FRONTEND PROJECT (Android Studio - Java)
│   ├── app/src/main/java/com/example/quizzapp/
│   │   ├── adapter/             # Các bộ Adapter quản lý danh bạ hiển thị
│   │   ├── api/                 # Retrofit Client & Định nghĩa API Endpoints (ApiService)
│   │   ├── auth/                # Các lớp hỗ trợ xác thực (GoogleAuthHelper)
│   │   ├── models/              # Lớp đối tượng ánh xạ dữ liệu JSON (QuestionResponse, AnswerOption)
│   │   └── ui/                  # Giao diện chính (QuizActivity, ResultActivity...)
│   └── app/src/main/res/
│       ├── drawable/            # Định nghĩa các Custom Background & Bo góc giao diện
│       └── layout/              # File thiết kế giao diện XML (activity_quiz_test, activity_result...)
└── .gitignore                   # Cấu hình bỏ qua các thư mục rác khi push lên Git

🛠️ Hướng dẫn Cài đặt & Chạy Thử
1. Khởi chạy Hệ thống Backend (be)
- Di chuyển vào thư mục backend:
  cd be

- Cài đặt toàn bộ các thư viện Node.js cần thiết:
  npm install

- Tạo file .env ở thư mục gốc của be và điền cấu hình kết nối database:
  PORT=3000
  MONGO_URI=mongodb://localhost:27017/quizzapp
  
- Khởi chạy server:
  npm run dev
(Server mặc định sẽ lắng nghe tại cổng http://localhost:3000)

2. Cấu hình và Chạy Ứng dụng Android (fe)
- Mở thư mục fe bằng công cụ Android Studio.
- Đảm bảo file cấu hình kết nối Firebase google-services.json đã được đặt chính xác trong thư mục fe/app/.
- Mở file ApiClient.java trong gói api/ và cấu hình lại địa chỉ Base URL trỏ về máy chủ:
- Nếu sử dụng Thiết bị giả lập (Emulator) mặc định: Điền http://10.0.2.2:3000/
- Nếu sử dụng Máy thật (Real Device): Điền chính xác địa chỉ IP mạng nội bộ (Wi-Fi) của máy tính.
- Nhấp vào nút Run App (Biểu tượng tam giác xanh) trên thanh công cụ để cài đặt và trải nghiệm ứng dụng.


📸 Giao diện Ứng dụng Thực tế

| Màn hình Đăng nhập | Màn hình Trang chủ | Màn hình Chỉnh sửa câu hỏi |
|---|---|---|
| ![Đăng nhập](fe/images/login.png) | ![Trang chủ](fe/images/main.png) | ![Chỉnh sửa câu hỏi](fe/images/edit.png) |

| Màn hình Thêm câu hỏi | Màn hình Làm bài Quiz | Màn hình Kết quả (Đúng/Sai) |
|---|---|---|
| ![Thêm câu hỏi](fe/images/add.png) | ![Làm bài quiz](fe/images/quiz.png) | ![Kết quả](fe/images/result.png) |


Thành viên Thực hiện
Học viên: Văn Đặng Tuyết Nga - Software Engineering
Đơn vị đào tạo: VTC Academy