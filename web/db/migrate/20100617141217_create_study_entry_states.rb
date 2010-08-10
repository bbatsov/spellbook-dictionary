class CreateStudyEntryStates < ActiveRecord::Migration
  def self.up
    create_table :study_entry_states do |t|
      t.string :name

      t.timestamps
    end
  end

  def self.down
    drop_table :study_entry_states
  end
end
