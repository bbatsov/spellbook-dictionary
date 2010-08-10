class CreateExamStates < ActiveRecord::Migration
  def self.up
    create_table :exam_states do |t|
      t.text :name

      t.timestamps
    end
  end

  def self.down
    drop_table :exam_states
  end
end
