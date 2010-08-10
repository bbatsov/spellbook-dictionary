class StudySet < ActiveRecord::Base

  validates_presence_of :name

  belongs_to :user
  belongs_to :dictioanry
  
  has_many :study_words
  has_many :study_sessions
  has_many :words, :through => :study_words

  def searched
    "type a word to search"
  end

  def searched=(word)
    @searched_word = word
  end

end
