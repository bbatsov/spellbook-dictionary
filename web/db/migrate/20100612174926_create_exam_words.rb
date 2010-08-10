class CreateExamWords < ActiveRecord::Migration
  def self.up
    create_table :exam_words do |t|
      t.integer :exam_id
      t.integer :word_id
      t.string :answered
      t.boolean :is_done, :default => false
      t.integer :number
      t.boolean :correct

      t.timestamps
    end
  end

  def self.down
    drop_table :exam_words
  end
end
