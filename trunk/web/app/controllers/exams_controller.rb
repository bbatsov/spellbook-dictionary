class ExamsController < ApplicationController
  #before_filter :require_user
  # GET /exams
  # GET /exams.xml
  def index
    @exams = Exam.all :conditions => { :user_id => current_user.id }

    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @exams }
    end
  end

  # GET /exams/1
  # GET /exams/1.xml
  def show
    @exam = Exam.find(params[:id])
    @service = ExamService.new(@exam)
        
    if (@exam.exam_state_id == ExamState.finished_id)
      render :partial => "results", :layout => "application"
      return
    end
    
    respond_to do |format|
      format.html # show.html.erb
      format.xml  { render :xml => @exam }
    end
  end

  # GET /exams/new
  # GET /exams/new.xml
  def new
    @exam = Exam.new
    @dictionaries = Dictionary.all
    @difficulties = Difficulty.all

    respond_to do |format|
      format.html # new.html.erb
      format.xml  { render :xml => @exam }
    end
  end

  # GET /exams/1/edit
  def edit
    @exam = Exam.find(params[:id])
  end

  # POST /exams
  # POST /exams.xml
  def create
    @exam = Exam.new(params[:exam])
    @exam.user_id = current_user.id

    respond_to do |format|
      if @exam.save
        flash[:notice] = 'Exam was successfully created.'
        format.html { redirect_to(@exam) }
        format.xml  { render :xml => @exam, :status => :created, :location => @exam }
      else
        format.html { render :action => "new" }
        format.xml  { render :xml => @exam.errors, :status => :unprocessable_entity }
      end
    end
  end

  # PUT /exams/1
  # PUT /exams/1.xml
  def update
    @exam = Exam.find(params[:id])

    respond_to do |format|
      if @exam.update_attributes(params[:exam])
        flash[:notice] = 'Exam was successfully updated.'
        format.html { redirect_to(@exam) }
        format.xml  { head :ok }
      else
        format.html { render :action => "edit" }
        format.xml  { render :xml => @exam.errors, :status => :unprocessable_entity }
      end
    end
  end

  # DELETE /exams/1
  # DELETE /exams/1.xml
  def destroy
    @exam = Exam.find(params[:id])
    @exam.destroy

    respond_to do |format|
      format.html { redirect_to(exams_url) }
      format.xml  { head :ok }
    end
  end

  def answer
    @exam = Exam.find(params[:id])
    
    @service = ExamService.new(@exam)
    
    @answer = params[:answer]
    
    @correct = @service.correct? @answer
    @service.move_next

    if @service.current_word
      respond_to do |format|
          format.js
       end
    else
      @service.close_exam
      render 'results'
    end
    
  end

  def change_state
    @exam = Exam.find(params[:id])
    @finished = params[:finished]

    respond_to do |format|
      format.js
    end
  end

end
