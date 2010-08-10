class Admin::SuggestionsController < ApplicationController
  before_filter :require_user, :admin_required
  
  def index
    @suggestions = Suggestion.all( :conditions => { :state_id => SuggestionState.created_id } )

    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @suggestions }
    end
  end

  def review
    @suggestion = Suggestion.find_by_id(params[:id])
    
    respond_to do |format|
      format.js
    end
  end

  def decide
    @suggestion = Suggestion.find_by_id(params[:id])

    if (params[:approved] == "1")
      @suggestion.state_id = SuggestionState.approved_id     
    else
      @suggestion.state_id = SuggestionState.rejected_id
    end

    @suggestion.comment = params["comment_#{@suggestion.id}"]
    @suggestion.user_id = current_user.id

    respond_to do |format|
      if @suggestion.save
          format.js
      else
          flash[:notice] = "Error saving your decision"
          format.html { render :action => "index" }
      end
    end
  end
  
end
