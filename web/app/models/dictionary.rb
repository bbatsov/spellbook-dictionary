class Dictionary < ActiveRecord::Base

  #belongs_to :language, :foreign_key => "language_from"
  belongs_to :source_language, :class_name => "Language", :foreign_key => "language_from"
  belongs_to :target_language, :class_name => "Language", :foreign_key => "language_to"

  has_many :words
  has_many :exams
  has_many :study_sets

  def searched
    "type a word to search"
  end

  def searched=(word)
    @searched_word = word
  end

end
