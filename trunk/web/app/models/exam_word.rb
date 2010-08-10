class ExamWord < ActiveRecord::Base

  belongs_to :exam
  belongs_to :word

end
