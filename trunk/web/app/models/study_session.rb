class StudySession < ActiveRecord::Base

  belongs_to :user
  belongs_to :study_set

  has_many :study_entries

  after_create :generate_study_entries

  def generate_study_entries
    
    @words = StudyWord.find :all, :conditions => { :study_set_id => self.study_set_id }

    @words.each do |study_word|
      entry = StudyEntry.new
      entry.study_word_id = study_word.id
      entry.study_session_id = self.id
      entry.study_entry_state_id = StudyEntryState.new_id
      entry.save
    end

  end

end
