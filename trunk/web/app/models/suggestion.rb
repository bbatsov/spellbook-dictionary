class Suggestion < ActiveRecord::Base

  belongs_to :word
  belongs_to :dictionary
  belongs_to :suggestion_state, :foreign_key => "state_id"
  has_many :suggestion_histories

  validates_presence_of :word_name, :translation

  before_create { |suggestion| suggestion.state_id = SuggestionState.created_id}

  after_save :save_suggestion_details

  attr_accessor :user_id
  attr_accessor :comment
  
  protected  
  def save_suggestion_details

    history = SuggestionHistory.new
    history.suggestion_id = self.id
    history.state_id = self.state_id
    history.user_id = self.user_id
    history.save

    if self.comment
      new_comment = SuggestionComment.new
      new_comment.suggestion_history_id = history.id
      new_comment.comment = self.comment
      new_comment.save
    end

    return unless self.state_id == SuggestionState.approved_id

    if self.word_id
      word = Word.find_by_id(word_id)
      word.word = self.word_name;
      word.translation = self.translation;
      word.save
      save_new_word_entry(word, 'updated')
    else
      word = Word.new
      word.word = self.word_name
      word.translation = self.translation
      word.dictionary_id = self.dictionary_id
      word.save
      save_new_word_entry(word, 'new')
    end
  end

  private
  def save_new_word_entry(word, state)
    new_word = NewWord.new
    new_word.word = word.word
    new_word.translation = word.translation
    new_word.dictionary = word.dictionary.name
    new_word.state = state
    new_word.save
  end
end
