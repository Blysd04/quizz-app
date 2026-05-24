import express from 'express';
import Question from '../models/question.model.js';
import { verifyToken } from '../middlewares/auth.middleware.js';

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

  // RÀNG BUỘC 1: Validation các trường bắt buộc
  if (!questionText || !options || !category) {
    return res.status(400).json({ message: 'Vui lòng điền đầy đủ các field bắt buộc (questionText, options, category)' });
  }

  // RÀNG BUỘC 2: Kiểm tra số lượng đáp án truyền lên
  if (!Array.isArray(options) || options.length < 2) {
    return res.status(400).json({ message: 'Câu hỏi phải có ít nhất 2 đáp án để lựa chọn' });
  }

  // RÀNG BUỘC 3: Phải có ít nhất 1 đáp án đúng (Cho phép chọn 1 hoặc nhiều đáp án đúng)
  const correctCount = options.filter(opt => opt.isCorrect === true).length;
  if (correctCount < 1) {
    return res.status(400).json({ message: 'Câu hỏi phải có ít nhất 1 đáp án được đánh dấu là ĐÚNG (isCorrect: true)' });
  }

  try {
    // RÀNG BUỘC 4: Chống trùng lặp câu hỏi khi tạo mới
    const existingQuestion = await Question.findOne({ 
      questionText: questionText.trim() 
    });

    if (existingQuestion) {
      return res.status(400).json({ message: 'Nội dung câu hỏi này đã tồn tại trên hệ thống, không được trùng lặp!' });
    }

    // Nếu vượt qua tất cả các ràng buộc, tiến hành lưu vào DB
    const newQuestion = new Question({
      userId: req.user.uid, // UID từ Firebase token
      questionText: questionText.trim(),
      options, // Nhận trực tiếp mảng đáp án linh hoạt từ Client gửi lên
      category
    });

    const savedQuestion = await newQuestion.save();
    res.status(201).json(savedQuestion);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// ==========================================
// 5. PUT - Chỉnh sửa câu hỏi (Chỉ chủ sở hữu công việc)
// ==========================================
router.put('/:id', verifyToken, async (req, res) => {
  const { questionText, options, category } = req.body;

  try {
    const question = await Question.findById(req.params.id);
    if (!question) return res.status(404).json({ message: 'Không tìm thấy câu hỏi' });

    // Kiểm tra phân quyền sở hữu dữ liệu
    if (question.userId !== req.user.uid) {
      return res.status(403).json({ message: 'Bạn không có quyền chỉnh sửa câu hỏi của người khác!' });
    }

    // RÀNG BUỘC KHI CHỈNH SỬA: Nếu người dùng có cập nhật mảng options (Thêm/Xóa bớt đáp án)
    if (options) {
      if (!Array.isArray(options) || options.length < 2) {
        return res.status(400).json({ message: 'Sau khi chỉnh sửa, câu hỏi vẫn phải đảm bảo có ít nhất 2 đáp án' });
      }

      // Vẫn phải đảm bảo giữ lại ít nhất 1 đáp án đúng (hoặc nhiều hơn)
      const correctCount = options.filter(opt => opt.isCorrect === true).length;
      if (correctCount < 1) {
        return res.status(400).json({ message: 'Sau khi chỉnh sửa, phải có ít nhất 1 đáp án đúng được giữ lại' });
      }
    }

    // RÀNG BUỘC: Chống trùng lặp câu hỏi khi chỉnh sửa nội dung chữ
    if (questionText && questionText.trim() !== question.questionText) {
      const duplicateQuestion = await Question.findOne({ 
        questionText: questionText.trim() 
      });
      
      if (duplicateQuestion) {
        return res.status(400).json({ message: 'Nội dung câu hỏi mới chỉnh sửa đã trùng với một câu hỏi khác trong hệ thống!' });
      }
    }

    // Tiến hành cập nhật dữ liệu mới vào MongoDB
    // Sử dụng $set để chỉ cập nhật các trường được gửi lên trong body
    const updatedQuestion = await Question.findByIdAndUpdate(
      req.params.id,
      { $set: req.body },
      { new: true, runValidators: true }
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

export default router;