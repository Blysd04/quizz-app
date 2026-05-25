import express from 'express';
import Question from '../models/question.model.js';
import { verifyToken } from '../middlewares/auth.middleware.js';
import { sendQuizNotification } from '../send-data-only.js';
import {sendWelcomeNotification} from '../middlewares/auth.middleware.js';
import mongoose from 'mongoose';

const router = express.Router();

// ==========================================
// 1. GET ALL - Lấy TẤT CẢ câu hỏi trên hệ thống (Dùng cho TRANG CHỦ)
// Không cần đăng nhập, ai cũng có thể xem để làm bài test
// ==========================================
router.get('/', async (req, res) => {
  try {
    const allQuestions = await Question.find().sort({ createdAt: -1 });
    res.json(allQuestions);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// ==========================================
// 2. GET BY USER - Chỉ lấy câu hỏi của chính user đang đăng nhập (Dùng cho trang QUẢN LÝ)
// Bắt buộc truyền Token để biết user là ai và lọc dữ liệu
// ==========================================
router.get('/my-questions', verifyToken, async (req, res) => {
  try {
    const userQuestions = await Question.find({ userId: req.user.uid }).sort({ createdAt: -1 });
    res.json(userQuestions);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// ==========================================
// 3. GET DETAIL - Xem chi tiết 1 câu hỏi theo ID (Dùng khi BẤM VÀO LÀM TEST)
// Không bắt buộc đăng nhập (hoặc tùy bạn), giúp lấy cấu hình đáp án để check đúng/sai
// ==========================================
router.get('/:id', async (req, res) => {
  try {
    const question = await Question.findById(req.params.id);
    if (!question) return res.status(404).json({ message: 'Không tìm thấy câu hỏi' });
    res.json(question);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// ==========================================
// 4. POST - Tạo mới câu hỏi (Bắt buộc đăng nhập)
// ==========================================
router.post('/', verifyToken, async (req, res) => {
    const { questionText, options, category } = req.body;

    if (!questionText || !options || !category) {
        return res.status(400).json({ message: 'Vui lòng điền đầy đủ thông tin' });
    }

    if (!Array.isArray(options) || options.length < 2) {
        return res.status(400).json({ message: 'Câu hỏi phải có ít nhất 2 đáp án' });
    }

    // Map dữ liệu để đảm bảo field 'answerText' luôn tồn tại
    const cleanedOptions = options.map(opt => ({
        answerText: opt.answerText ? opt.answerText.trim() : "",
        isCorrect: !!opt.isCorrect
    }));

    if (cleanedOptions.some(opt => opt.answerText === "")) {
        return res.status(400).json({ message: 'Tất cả các đáp án phải có nội dung!' });
    }

    const correctCount = cleanedOptions.filter(opt => opt.isCorrect === true).length;
    if (correctCount < 1) {
        return res.status(400).json({ message: 'Phải có ít nhất 1 đáp án đúng' });
    }

    try {
        const existingQuestion = await Question.findOne({ questionText: questionText.trim() });
        if (existingQuestion) {
            return res.status(400).json({ message: 'Câu hỏi đã tồn tại!' });
        }

        const newQuestion = new Question({
            userId: req.user.uid,
            questionText: questionText.trim(),
            options: cleanedOptions,
            category: category
        });

        const savedQuestion = await newQuestion.save();
        res.status(201).json(savedQuestion);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// 5. PUT - Chỉnh sửa
router.put('/:id', verifyToken, async (req, res) => {
    const { questionText, options, category } = req.body;

    try {
        const question = await Question.findById(req.params.id);
        if (!question) return res.status(404).json({ message: 'Không tìm thấy câu hỏi' });

        if (question.userId !== req.user.uid) {
            return res.status(403).json({ message: 'Bạn không có quyền chỉnh sửa!' });
        }

        let updateData = { questionText, category };

        if (options) {
            const cleanedOptions = options.map(opt => ({
                answerText: opt.answerText ? opt.answerText.trim() : "",
                isCorrect: !!opt.isCorrect
            }));
            updateData.options = cleanedOptions;
        }

        const updatedQuestion = await Question.findByIdAndUpdate(
            req.params.id,
            { $set: updateData },
            { returnDocument: 'after', runValidators: true }
        );
        
        res.json(updatedQuestion);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// ==========================================
// 6. DELETE - Xóa câu hỏi (Chỉ chủ sở hữu)
// ==========================================
router.delete('/:id', verifyToken, async (req, res) => {
  try {
    const question = await Question.findById(req.params.id);
    if (!question) return res.status(404).json({ message: 'Không tìm thấy câu hỏi' });

    if (question.userId !== req.user.uid) {
      return res.status(403).json({ message: 'Bạn không có quyền xóa câu hỏi của người khác!' });
    }

    await question.deleteOne();
    res.json({ message: 'Xóa câu hỏi thành công!' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

router.post("/send-quiz", async (req, res) => {
    // Lấy questionId gửi lên từ request body hoặc query 
    const { questionId, userFcmToken } = req.body; 

    // Gọi hàm linh động, truyền giá trị thực tế vào
    sendQuizNotification(userFcmToken, questionId);

    res.json({ message: "Đã kích hoạt gửi thông báo câu hỏi " + questionId });
});

router.post('/me/fcm-token', verifyToken, async (req, res) => {
    try {
        // Lấy UID từ Firebase ID Token đã giải mã được gán vào req.user ở middleware verifyToken
        const uid = req.user.uid; 
        
        // Hứng fcmToken gửi từ Android lên (khớp với key "fcmToken" phía Android)
        const { fcmToken } = req.body; 
        

        if (!fcmToken) {
            return res.status(400).json({ message: "Thiếu fcmToken trong request body!" });
        }

        // Cập nhật trường fcmToken vào TẤT CẢ các câu hỏi thuộc về userId này trong collection 'questions'
        const result = await mongoose.connection.collection('questions').updateMany(
            { userId: uid }, 
            { $set: { fcmToken: fcmToken, tokenUpdatedAt: new Date() } }
        );

        sendWelcomeNotification(uid).catch(err => console.error(err));

        return res.status(200).json({ 
            message: "Đã lưu FCM Token vào các câu hỏi thành công!",
            matchedCount: result.matchedCount,
            modifiedCount: result.modifiedCount
        });
        
    } catch (error) {
        console.error("Lỗi khi lưu FCM Token vào collection questions:", error);
        return res.status(500).json({ 
            message: "Lỗi Server khi lưu Token", 
            error: error.message 
        });
    }
});

export default router;