# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_Spellbook_session',
  :secret      => 'd3cc6b8d627a20c94e660aaea64fa9e3a1a23f2d2e06cd9456ad4fc1412bec9f70d8ddb98b1584b03ca35bf46bb5c4440535050a14275ee7fe0fbac1d9497206'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
