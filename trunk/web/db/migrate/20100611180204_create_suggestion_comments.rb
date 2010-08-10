class CreateSuggestionComments < ActiveRecord::Migration
  def self.up
    create_table :suggestion_comments do |t|
      t.integer :suggestion_history_id
      t.text :comment

      t.timestamps
    end
  end

  def self.down
    drop_table :suggestion_comments
  end
end
