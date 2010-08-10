class StudyEntry < ActiveRecord::Base

  belongs_to :study_word
  belongs_to :study_session
  belongs_to :study_entry_state

end
