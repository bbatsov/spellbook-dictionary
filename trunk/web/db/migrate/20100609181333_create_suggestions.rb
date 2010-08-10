class CreateSuggestions < ActiveRecord::Migration
  def self.up
    create_table :suggestions do |t|
      t.text :word_name, :null => false
      t.text :translation, :null => false
      t.integer :state_id
      t.integer :word_id
      t.integer :dictionary_id

      t.timestamps
    end

   # execute <<-SQL
    # ALTER TABLE suggestions
     #   ADD CONSTRAINT ]fk_created_by'
      #  FOREIGN KEY (created_by)
       # REFERENCES users(id)

     # ALTER TABLE suggestions
      #  ADD CONSTRAINT fk_approved_by
       # FOREIGN KEY (approved_by)
        #REFERENCES users(id)

      #ALTER TABLE suggestions
       # ADD CONSTRAINT fk_word
        #FOREIGN KEY (word_id)
        #REFERENCES words(id)
    #SQL
  end

  def self.down
    #execute "ALTER TABLE suggestions DROP FOREIGN KEY fk_created_by"
    #execute "ALTER TABLE suggestions DROP FOREIGN KEY fk_approved_by"
    #execute "ALTER TABLE suggestions DROP FOREIGN KEY fk_word"
    drop_table :suggestions
  end
end
