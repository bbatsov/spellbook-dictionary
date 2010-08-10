class SuggestionHistoriesController < ApplicationController
  # GET /suggestion_histories
  # GET /suggestion_histories.xml
  def index
    @suggestion_histories = SuggestionHistory.all

    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @suggestion_histories }
    end
  end

  # GET /suggestion_histories/1
  # GET /suggestion_histories/1.xml
  def show
    @suggestion_history = SuggestionHistory.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.xml  { render :xml => @suggestion_history }
    end
  end

  # GET /suggestion_histories/new
  # GET /suggestion_histories/new.xml
  def new
    @suggestion_history = SuggestionHistory.new

    respond_to do |format|
      format.html # new.html.erb
      format.xml  { render :xml => @suggestion_history }
    end
  end

  # GET /suggestion_histories/1/edit
  def edit
    @suggestion_history = SuggestionHistory.find(params[:id])
  end

  # POST /suggestion_histories
  # POST /suggestion_histories.xml
  def create
    @suggestion_history = SuggestionHistory.new(params[:suggestion_history])

    respond_to do |format|
      if @suggestion_history.save
        flash[:notice] = 'SuggestionHistory was successfully created.'
        format.html { redirect_to(@suggestion_history) }
        format.xml  { render :xml => @suggestion_history, :status => :created, :location => @suggestion_history }
      else
        format.html { render :action => "new" }
        format.xml  { render :xml => @suggestion_history.errors, :status => :unprocessable_entity }
      end
    end
  end

  # PUT /suggestion_histories/1
  # PUT /suggestion_histories/1.xml
  def update
    @suggestion_history = SuggestionHistory.find(params[:id])

    respond_to do |format|
      if @suggestion_history.update_attributes(params[:suggestion_history])
        flash[:notice] = 'SuggestionHistory was successfully updated.'
        format.html { redirect_to(@suggestion_history) }
        format.xml  { head :ok }
      else
        format.html { render :action => "edit" }
        format.xml  { render :xml => @suggestion_history.errors, :status => :unprocessable_entity }
      end
    end
  end

  # DELETE /suggestion_histories/1
  # DELETE /suggestion_histories/1.xml
  def destroy
    @suggestion_history = SuggestionHistory.find(params[:id])
    @suggestion_history.destroy

    respond_to do |format|
      format.html { redirect_to(suggestion_histories_url) }
      format.xml  { head :ok }
    end
  end
end
