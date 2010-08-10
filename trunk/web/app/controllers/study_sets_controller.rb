class StudySetsController < ApplicationController
  before_filter :require_user
  # GET /study_sets
  # GET /study_sets.xml
  def index
    @study_sets = StudySet.all :conditions => { :user_id => current_user.id }

    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @study_sets }
    end
  end

  # GET /study_sets/1
  # GET /study_sets/1.xml
  def show
    @study_set = StudySet.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.xml  { render :xml => @study_set }
    end
  end

  # GET /study_sets/new
  # GET /study_sets/new.xml
  def new
    @study_set = StudySet.new
    @dictionaries = Dictionary.all

    respond_to do |format|
      format.html # new.html.erb
      format.xml  { render :xml => @study_set }
    end
  end

  # GET /study_sets/1/edit
  def edit
    @study_set = StudySet.find(params[:id])
    @dictionaries = Dictionary.all
  end

  # POST /study_sets
  # POST /study_sets.xml
  def create
    @study_set = StudySet.new(params[:study_set])
    @study_set.user_id = current_user.id

    respond_to do |format|
      if @study_set.save
        flash[:notice] = 'StudySet was successfully created.'
        format.html { redirect_to(@study_set) }
        format.xml  { render :xml => @study_set, :status => :created, :location => @study_set }      
      end
    end
  end

  # PUT /study_sets/1
  # PUT /study_sets/1.xml
  def update
    @study_set = StudySet.find(params[:id])
    @study_set.user_id = current_user.id

    respond_to do |format|
      if @study_set.update_attributes(params[:study_set])
        flash[:notice] = 'StudySet was successfully updated.'
        format.html { redirect_to(@study_set) }
        format.xml  { head :ok }
      else
        format.html { render :action => "edit" }
        format.xml  { render :xml => @study_set.errors, :status => :unprocessable_entity }
      end
    end
  end

  # DELETE /study_sets/1
  # DELETE /study_sets/1.xml
  def destroy
    @study_set = StudySet.find(params[:id])
    @study_set.destroy

    respond_to do |format|
      format.html { redirect_to(study_sets_url) }
      format.xml  { head :ok }
    end
  end

  def remove_word
    @study_word = StudyWord.find(params[:id])
    @study_word.delete

    respond_to do |format|
      format.js
    end
    
  end

  def add_word
    @study_set = StudySet.find(params[:id])    
    @word = Word.find_by_word_and_dictionary_id(params[:study_set][:searched], @study_set.dictionary_id)

    if @word
      @study_word = StudyWord.new
      @study_word.study_set_id = @study_set.id
      @study_word.word_id = @word.id
      
      max = StudyWord.maximum("number", :conditions => { :study_set_id => @study_set.id })
      max = 0 unless max
      @study_word.number = max + 1;

      @study_word.save

    end

    respond_to do |format|
      format.js
    end

  end  
end
