class CreateSuggestionHistories < ActiveRecord::Migration
  def self.up
    create_table :suggestion_histories do |t|
      t.integer :suggestion_id
      t.integer :state_id
      t.integer :user_id

      t.timestamps
    end
  end

  def self.down
    drop_table :suggestion_histories
  end
end
