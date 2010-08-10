class CreateSuggestionStates < ActiveRecord::Migration
  def self.up
    create_table :suggestion_states do |t|
      t.text :name

      t.timestamps
    end
  end

  def self.down
    drop_table :suggestion_states
  end
end
