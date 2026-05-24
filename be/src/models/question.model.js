import mongoose from 'mongoose';

const answerOptionSchema = new mongoose.Schema({
  answerText: { type: String, required: true },
  isCorrect: { type: Boolean, required: true, default: false }
});

const questionSchema = new mongoose.Schema({
  userId: { type: String, required: true },
  questionText: { type: String, required: true },
  // Cho phép linh hoạt số lượng đáp án (mảng chứa nhiều object đáp án)
  options: [answerOptionSchema], 
  category: { type: String, required: true }
}, { timestamps: true });

export default mongoose.model('Question', questionSchema);