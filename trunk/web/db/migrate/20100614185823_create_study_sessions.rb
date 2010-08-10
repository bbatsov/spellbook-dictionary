class CreateStudySessions < ActiveRecord::Migration
  def self.up
    create_table :study_sessions do |t|
      t.integer :user_id
      t.integer :study_set_id
      t.integer :correct
      t.integer :seen
      t.boolean :finished

      t.timestamps
    end
  end

  def self.down
    drop_table :study_sessions
  end
end
