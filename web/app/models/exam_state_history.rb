class ExamStateHistory < ActiveRecord::Base

  belongs_to :exam
  belongs_to :exam_state

end
