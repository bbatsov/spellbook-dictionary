class SuggestionState < ActiveRecord::Base

  has_many :suggestions
  has_many :suggestion_histories

  @@created = SuggestionState.find_by_name("created")
  @@approved = SuggestionState.find_by_name("approved")
  @@rejected = SuggestionState.find_by_name("rejected")
  @@reopen = SuggestionState.find_by_name("reopen")

  def self.created_id
    @@created.id
  end

  def self.approved_id
    @@approved.id
  end

  def self.rejected_id
    @@rejected.id
  end

  def self.reopen_id
    @@reopen.id
  end

end
