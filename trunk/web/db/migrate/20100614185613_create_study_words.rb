class CreateStudyWords < ActiveRecord::Migration
  def self.up
    create_table :study_words do |t|
      t.integer :word_id
      t.integer :study_set_id
      t.integer :number

      t.timestamps
    end
  end

  def self.down
    drop_table :study_words
  end
end
