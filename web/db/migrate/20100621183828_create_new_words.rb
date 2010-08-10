class CreateNewWords < ActiveRecord::Migration
  def self.up
    create_table :new_words do |t|
      t.text :word
      t.text :translation
      t.text :dictionary
      t.text :state

      t.timestamps
    end
  end

  def self.down
    drop_table :new_words
  end
end
