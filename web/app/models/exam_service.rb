class ExamService

  def initialize(exam)
    @exam = exam
    
    @exam_words = @exam.exam_words #ExamWord.find :all, :conditions => { :exam_id => @exam.id }#ExamWord.find_by_exam_id(@exam.id)
    @current_word = @exam_words.detect { |w| !w.is_done }
  end

  def current_word
    @current_word
  end

  def exam_words
    @exam_words
  end

  def exam
    @exam
  end

  def correct?(answer)
    @word = @current_word.word #Word.find_by_id(@current_word.word_id)

    @current_word.correct = (answer.length > 0) && (@word.translation.downcase.include? answer.downcase)
    @current_word.answered = answer
    @current_word.is_done = true

    @current_word.save
    
    @current_word.correct
  end

  def move_next
    next_word = @exam_words.select { |w| w.number == @current_word.number + 1 }
    if next_word.empty?
      @current_word = nil
    else
      @current_word = next_word[0]
    end
  end

  def close_exam
    correct = (@exam_words.select { |w| w.correct }).size
    @exam.score = correct *100 / @exam.size
    change_exam_state ExamState.finished_id
  end

  def pause_exam
    change_exam_state ExamState.paused_id
  end

  def resume_exam
    change_exam_state ExamState.started_id
  end

  private
  def change_exam_state state    
    @exam.exam_state_id = state
    @exam.save
  end
end
