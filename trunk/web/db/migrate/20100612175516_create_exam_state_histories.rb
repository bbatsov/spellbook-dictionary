class CreateExamStateHistories < ActiveRecord::Migration
  def self.up
    create_table :exam_state_histories do |t|
      t.integer :exam_id
      t.integer :exam_state_id

      t.timestamps
    end
  end

  def self.down
    drop_table :exam_state_histories
  end
end
