class CreateWordRanks < ActiveRecord::Migration
  def self.up
    create_table :word_ranks do |t|
      t.integer :word_id
      t.integer :rank

      t.timestamps
    end
  end

  def self.down
    drop_table :word_ranks
  end
end
