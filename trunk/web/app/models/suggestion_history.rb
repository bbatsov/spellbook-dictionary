class SuggestionHistory < ActiveRecord::Base

  belongs_to :suggestion_state
  belongs_to :user
  belongs_to :suggestion

  has_one :suggestion_comment

end
