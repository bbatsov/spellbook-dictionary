class Exam < ActiveRecord::Base
  
  belongs_to :user
  belongs_to :dictionary
  belongs_to :difficulty
  belongs_to :exam_state

  has_many :exam_words
  has_many :exam_state_histories

  before_create :init_exam
  after_create :generate_exam_words
  after_save :save_exam_history

  def save_exam_history    
    if self.exam_state_id_changed?
      history = ExamStateHistory.new
      history.exam_id = self.id
      history.exam_state_id = self.exam_state_id
      history.save
    end
  end

  def init_exam
    self.exam_state_id = ExamState.started_id
    self.score = 0;
  end

  def generate_exam_words
    diff = Difficulty.find_by_id(self.difficulty_id)
    max_id = Word.maximum("id")
    exam_words = [];
    number = 1;

    while (exam_words.count < self.size)
      id = 1 + rand(max_id)
      found = Word.find_by_id(id)

      next if !found
      
      if found.dictionary_id == self.dictionary_id
        exam_word = ExamWord.new;
        exam_word.word_id = found.id
        exam_word.exam_id = self.id
        exam_word.number = number
        exam_words << exam_word
        number += 1
      end
      #
      #next if !found or !found.word_rank
      #self.exam_words << found if (found.dictionary_id == self.dictionary_id and found.word_rank.rank < diff.rank_to and found.word_rank >= diff.rank_from)
    end

    exam_words.each { |e| e.save  }
    
  end
  
end
