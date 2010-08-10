class WordsController < ApplicationController
  
  def index
    if params[:dictionary_id]
      @words = Word.find(:all, :limit => 20, :conditions => ['word LIKE ? and dictionary_id = ?', "#{params[:search]}%", params[:dictionary_id]])
    else
      @words = Word.find(:all, :limit => 20, :conditions => ['word LIKE ?', "#{params[:search]}%"])
    end
  end

end
