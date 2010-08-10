class StudyService

  def initialize(study_session)
    @study_session = study_session
    @study_entries = @study_session.study_entries
    @current_entry = @study_entries.detect { |e| e.study_entry_state_id == StudyEntryState.new_id }
  end

  def current_entry
    @current_entry
  end

  def study_entries
    @study_entries
  end

  def study_session
    @study_session
  end

  def correct?(answer)
    @word = @current_entry.study_word.word #Word.find_by_id(@current_word.word_id)
    correct = (answer.length > 0) && (@word.translation.downcase.include? answer.downcase)
    @current_entry.study_entry_state_id = correct ? StudyEntryState.correct_id : StudyEntryState.wrong_id
    @current_entry.answered = answer
    @current_entry.save
    #@study_entries = @study_session.study_entries(true)
    correct
  end

  def set_correct_count
    correct_count = (@study_entries.select { |e| e.study_entry_state_id == StudyEntryState.correct_id }).size
    @study_session.correct = correct_count * 100 / @study_entries.size
    @study_session.save
  end

  def mark_seen
    @current_entry.study_entry_state_id = StudyEntryState.seen_id
    @current_entry.save
    @seen = true
    @seen_entry = @current_entry
  end

  def seen?
    @seen
  end

  def seen_entry
    @seen_entry
  end

  def move_next
    @current_entry = @study_entries.detect { |e| e.study_entry_state_id == StudyEntryState.new_id }
  end
  
end
