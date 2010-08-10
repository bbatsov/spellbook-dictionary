class Language < ActiveRecord::Base

  #has_many :dictionary, :foreign_key => "language_from"
  has_many :source_dictionaries, :class_name => "Dictionary", :foreign_key => "language_from"
  has_many :target_dictionaries, :class_name => "Dictionary", :foreign_key => "language_to"

end
