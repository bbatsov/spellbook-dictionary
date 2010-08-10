class CreateDictionaries < ActiveRecord::Migration
  def self.up
    create_table :dictionaries do |t|
      t.text :name

      t.integer :language_from
      t.integer :language_to
      
      t.timestamps
    end
  end

  def self.down
    drop_table :dictionaries
  end
end
