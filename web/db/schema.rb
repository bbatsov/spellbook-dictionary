# This file is auto-generated from the current state of the database. Instead of editing this file, 
# please use the migrations feature of Active Record to incrementally modify your database, and
# then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your database schema. If you need
# to create the application database on another system, you should be using db:schema:load, not running
# all the migrations from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20100621183828) do

  create_table "dictionaries", :force => true do |t|
    t.text     "name"
    t.integer  "language_from"
    t.integer  "language_to"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "dictionaries", ["language_from"], :name => "fk_dictionaries_languages"
  add_index "dictionaries", ["language_to"], :name => "fk_dictionaries_languages1"

  create_table "difficulties", :force => true do |t|
    t.text     "name"
    t.integer  "rank_from"
    t.integer  "rank_to"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "exam_state_histories", :force => true do |t|
    t.integer  "exam_id"
    t.integer  "exam_state_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "exam_state_histories", ["exam_id"], :name => "fk_exam_state_histories_exams1"
  add_index "exam_state_histories", ["exam_state_id"], :name => "fk_exam_state_histories_exam_states1"

  create_table "exam_states", :force => true do |t|
    t.text     "name"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "exam_words", :force => true do |t|
    t.integer  "exam_id"
    t.integer  "word_id"
    t.integer  "number"
    t.text     "answered"
    t.boolean  "is_done",    :default => false
    t.boolean  "correct"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "exam_words", ["exam_id"], :name => "fk_exam_words_exams1"
  add_index "exam_words", ["word_id"], :name => "fk_exam_words_words1"

  create_table "exams", :force => true do |t|
    t.integer  "user_id"
    t.integer  "dictionary_id"
    t.integer  "size"
    t.integer  "difficulty_id"
    t.integer  "score"
    t.integer  "exam_state_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "exams", ["dictionary_id"], :name => "fk_exams_dictionaries1"
  add_index "exams", ["difficulty_id"], :name => "fk_exams_difficulties1"
  add_index "exams", ["exam_state_id"], :name => "fk_exams_exam_states1"
  add_index "exams", ["user_id"], :name => "fk_exams_users1"

  create_table "languages", :force => true do |t|
    t.text     "name"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "new_words", :force => true do |t|
    t.text     "word"
    t.text     "translation"
    t.text     "dictionary"
    t.text     "state"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "study_entries", :force => true do |t|
    t.integer  "study_word_id"
    t.integer  "study_session_id",     :limit => 11
    t.integer  "study_entry_state_id"
    t.text     "answered"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "study_entries", ["study_entry_state_id"], :name => "fk_study_entries_study_entry_states1"
  add_index "study_entries", ["study_session_id"], :name => "fk_study_entries_study_sessions1"
  add_index "study_entries", ["study_word_id"], :name => "fk_study_entries_study_words1"

  create_table "study_entry_states", :force => true do |t|
    t.string   "name"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "study_sessions", :force => true do |t|
    t.integer  "user_id"
    t.integer  "study_set_id"
    t.integer  "correct"
    t.integer  "seen"
    t.boolean  "finished"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "study_sessions", ["study_set_id"], :name => "fk_study_sessions_study_sets1"
  add_index "study_sessions", ["user_id"], :name => "fk_study_sessions_users1"

  create_table "study_sets", :force => true do |t|
    t.integer  "dictionary_id", :limit => 11
    t.integer  "user_id"
    t.text     "name"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "study_sets", ["dictionary_id"], :name => "fk_study_sets_dictionaries1"
  add_index "study_sets", ["user_id"], :name => "fk_study_sets_users1"

  create_table "study_words", :force => true do |t|
    t.integer  "word_id"
    t.integer  "study_set_id", :limit => 11
    t.integer  "number"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "study_words", ["study_set_id"], :name => "fk_study_words_study_sets1"
  add_index "study_words", ["word_id"], :name => "fk_study_words_words1"

  create_table "suggestion_comments", :force => true do |t|
    t.integer  "suggestion_history_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.text     "comment"
  end

  add_index "suggestion_comments", ["suggestion_history_id"], :name => "fk_suggestion_comments_suggestion_histories1"

  create_table "suggestion_histories", :force => true do |t|
    t.integer  "suggestion_id"
    t.integer  "state_id"
    t.integer  "user_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "suggestion_histories", ["state_id"], :name => "fk_suggestion_histories_suggestion_states1"
  add_index "suggestion_histories", ["suggestion_id"], :name => "fk_suggestion_histories_suggestions1"
  add_index "suggestion_histories", ["user_id"], :name => "fk_suggestion_histories_users1"

  create_table "suggestion_states", :force => true do |t|
    t.text     "name"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "suggestions", :force => true do |t|
    t.text     "word_name",                   :null => false
    t.text     "translation",                 :null => false
    t.integer  "state_id"
    t.integer  "word_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer  "dictionary_id", :limit => 11
  end

  add_index "suggestions", ["dictionary_id"], :name => "fk_suggestions_dictionaries1"
  add_index "suggestions", ["state_id"], :name => "fk_suggestions_suggestion_states1"
  add_index "suggestions", ["word_id"], :name => "fk_suggestions_words1"

  create_table "user_sessions", :force => true do |t|
    t.string   "session_id", :null => false
    t.text     "data"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "user_sessions", ["session_id"], :name => "index_sessions_on_session_id"
  add_index "user_sessions", ["updated_at"], :name => "index_sessions_on_updated_at"

  create_table "users", :force => true do |t|
    t.string   "login",                                  :null => false
    t.string   "email",                                  :null => false
    t.string   "crypted_password",                       :null => false
    t.string   "password_salt",                          :null => false
    t.string   "persistence_token",                      :null => false
    t.string   "single_access_token",                    :null => false
    t.string   "perishable_token",                       :null => false
    t.integer  "login_count",         :default => 0,     :null => false
    t.integer  "failed_login_count",  :default => 0,     :null => false
    t.datetime "last_request_at"
    t.datetime "current_login_at"
    t.datetime "last_login_at"
    t.string   "current_login_ip"
    t.string   "last_login_ip"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.boolean  "is_admin",            :default => false
  end

  create_table "word_ranks", :force => true do |t|
    t.integer  "word_id",    :limit => 11
    t.integer  "rank"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "word_ranks", ["word_id"], :name => "fk_word_ranks_words1"

  create_table "words", :force => true do |t|
    t.text     "word"
    t.text     "translation"
    t.integer  "dictionary_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "words", ["dictionary_id"], :name => "dictionary_index"
  add_index "words", ["dictionary_id"], :name => "fk_words_dictionaries1"
  add_index "words", ["word"], :name => "word_index"

end
