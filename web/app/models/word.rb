class Word < ActiveRecord::Base

  belongs_to :dictionary

  has_many :suggestions
  has_many :exam_words
  has_many :study_words

  has_one :word_rank

end
