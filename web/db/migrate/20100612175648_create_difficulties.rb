class CreateDifficulties < ActiveRecord::Migration
  def self.up
    create_table :difficulties do |t|
      t.integer :name
      t.integer :rank_from
      t.integer :rank_to

      t.timestamps
    end
  end

  def self.down
    drop_table :difficulties
  end
end
