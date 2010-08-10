class StudyWord < ActiveRecord::Base

  belongs_to :word
  belongs_to :study_set

  has_many :study_entries

end
