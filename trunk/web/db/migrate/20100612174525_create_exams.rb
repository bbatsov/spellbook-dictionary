class CreateExams < ActiveRecord::Migration
  def self.up
    create_table :exams do |t|
      t.integer :user_id
      t.integer :dictionary_id
      t.integer :size
      t.integer :difficulty_id
      t.integer :score
      t.integer :exam_state_id

      t.timestamps
    end
  end

  def self.down
    drop_table :exams
  end
end
