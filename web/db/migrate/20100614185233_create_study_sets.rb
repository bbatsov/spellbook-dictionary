class CreateStudySets < ActiveRecord::Migration
  def self.up
    create_table :study_sets do |t|
      t.integer :dictionary_id
      t.integer :user_id
      t.text :name

      t.timestamps
    end
  end

  def self.down
    drop_table :study_sets
  end
end
