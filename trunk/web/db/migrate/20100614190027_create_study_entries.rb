class CreateStudyEntries < ActiveRecord::Migration
  def self.up
    create_table :study_entries do |t|
      t.integer :study_word_id
      t.integer :study_session_id
      t.integer :study_entry_state_id
      t.string :answered

      t.timestamps
    end
  end

  def self.down
    drop_table :study_entries
  end
end
