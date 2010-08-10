class ExamState < ActiveRecord::Base

  has_many :exams
  has_many :exam_state_histories

  @@started = ExamState.find_by_name("started")
  @@paused = ExamState.find_by_name("paused")
  @@finised = ExamState.find_by_name("finished")

  def self.started_id
    @@started.id
  end

  def self.paused_id
    @@paused.id
  end

  def self.finished_id
    @@finised.id
  end

end
