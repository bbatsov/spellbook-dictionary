class StudySessionsController < ApplicationController
  before_filter :require_user
  # GET /study_sessions
  # GET /study_sessions.xml
  def index
    @study_sessions = StudySession.all :conditions => { :user_id => current_user.id }

    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @study_sessions }
    end
  end

  # GET /study_sessions/1
  # GET /study_sessions/1.xml
  def show
    @study_session = StudySession.find(params[:id])
    @service = StudyService.new(@study_session)

    if @service.current_entry.nil?
      render :partial => "results", :layout => "application"
      return
    end

    respond_to do |format|
      format.html # show.html.erb
      format.xml  { render :xml => @study_session }
    end
  end

  # GET /study_sessions/new
  # GET /study_sessions/new.xml
  def new
    @study_session = StudySession.new
    @set = StudySet.find :all, :conditions => { :user_id => current_user.id }

    respond_to do |format|
      format.html # new.html.erb
      format.xml  { render :xml => @study_session }
    end
  end

  # GET /study_sessions/1/edit
  def edit
    @study_session = StudySession.find(params[:id])
  end

  # POST /study_sessions
  # POST /study_sessions.xml
  def create
    @study_session = StudySession.new(params[:study_session])
    @study_session.user_id = current_user.id
    @study_session.correct=0;
    @study_session.seen=0;

    respond_to do |format|
      if @study_session.save
        flash[:notice] = 'StudySession was successfully created.'
        format.html { redirect_to(@study_session) }
        format.xml  { render :xml => @study_session, :status => :created, :location => @study_session }
      else
        format.html { render :action => "new" }
        format.xml  { render :xml => @study_session.errors, :status => :unprocessable_entity }
      end
    end
  end

  # PUT /study_sessions/1
  # PUT /study_sessions/1.xml
  def update
    @study_session = StudySession.find(params[:id])

    respond_to do |format|
      if @study_session.update_attributes(params[:study_session])
        flash[:notice] = 'StudySession was successfully updated.'
        format.html { redirect_to(@study_session) }
        format.xml  { head :ok }
      else
        format.html { render :action => "edit" }
        format.xml  { render :xml => @study_session.errors, :status => :unprocessable_entity }
      end
    end
  end

  # DELETE /study_sessions/1
  # DELETE /study_sessions/1.xml
  def destroy
    @study_session = StudySession.find(params[:id])
    @study_session.destroy

    respond_to do |format|
      format.html { redirect_to(study_sessions_url) }
      format.xml  { head :ok }
    end
  end

   def answer
    @study_session = StudySession.find(params[:id])
    @service = StudyService.new(@study_session)

    @answer = params[:answer]

    @correct = @service.correct? @answer
    @service.move_next

    if @service.current_entry
      respond_to do |format|
          format.js
       end
    else
      @service.set_correct_count
      render 'results'
    end
  end

  def see
    @study_session = StudySession.find(params[:id])
    @service = StudyService.new(@study_session)
    @service.mark_seen
    @service.move_next

    if @service.current_entry
      respond_to do |format|
          format.js
       end
    else
      @service.set_correct_count
      render 'results'
    end
  end
  
end
