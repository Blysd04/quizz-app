import express from 'express';
import mongoose from 'mongoose';
import cors from 'cors';
import dotenv from 'dotenv';
import questionRoutes from './routes/question.route.js';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middlewares cơ bản
app.use(cors());
app.use(express.json()); // Để server đọc được dữ liệu JSON gửi lên từ App/Postman

// Đăng ký Route
app.use('/api/questions', questionRoutes);

// Kết nối MongoDB và Chạy Server
mongoose.connect(process.env.MONGODB_URI)
  .then(() => {
    console.log('☘️ Kết nối cơ sở dữ liệu MongoDB thành công!');
    app.listen(PORT, () => {
      console.log(`🚀 Server đang chạy mượt mà tại port: ${PORT}`);
    });
  })
  .catch((err) => {
    console.error('❌ Lỗi kết nối MongoDB:', err.message);
  });