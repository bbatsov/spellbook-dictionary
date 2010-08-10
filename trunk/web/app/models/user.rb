class User < ActiveRecord::Base
  acts_as_authentic

  has_many :suggestion_histories
  has_many :exams
  has_many :study_sets
  has_many :study_sessions
    
end