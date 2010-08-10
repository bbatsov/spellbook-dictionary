class DictionariesController < ApplicationController
  #before_filter :require_user
  layout "application", :except => :search

  def index
    @dictionaries = Dictionary.all

    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @dictionaries }
    end
  end

  def show
    @dictionary = Dictionary.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.xml  { render :xml => @dictionary }
    end
  end

  def edit
    @dictionary = Dictionary.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.xml  { render :xml => @dictionary }
    end
  end

  def search

    @exact_match = Word.find(:all, :conditions => ['word = ? and dictionary_id = ?', params['dictionary']['searched'], params['id']])
    @similars = Word.find(:all, :limit => 20, :conditions => ['word LIKE ? and dictionary_id = ?', "#{params['dictionary']['searched']}%", params['id']])
    @searched_word = params['dictionary']['searched']
    @dictionary_id = params['id']
    
    respond_to do |format|
      format.html
      format.js
    end

  end

end
