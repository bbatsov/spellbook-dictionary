class Admin::UsersController < ApplicationController    
  layout "users"
  
  
  before_filter :require_user, :admin_required
  
  def index
    @users = User.all

    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @suggestions }
    end
  end

  def edit
    @user = User.find_by_id(params[:id])
    
    respond_to do |format|
      format.html # edit.html.erb
      format.xml  { render :xml => @suggestions }
    end
    
  end

  def update
    @user = User.find_by_id(params[:id])
    
    if @user.update_attributes(params[:user])
      flash[:notice] = "Account updated!"
      redirect_to admin_users_path
    else
      render :action => :edit
    end
  end
  
end
