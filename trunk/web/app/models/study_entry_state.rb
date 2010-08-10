class StudyEntryState < ActiveRecord::Base

  has_many :study_entries

  @@correct = StudyEntryState.find_by_name("correct")
  @@wrong = StudyEntryState.find_by_name("wrong")
  @@seen = StudyEntryState.find_by_name("seen")
  @@new = StudyEntryState.find_by_name("new")

  def self.correct_id
    @@correct.id
  end

  def self.wrong_id
    @@wrong.id
  end

  def self.seen_id
    @@seen.id
  end

  def self.new_id
    @@new.id
  end

end
