class Admin::HomeController < ApplicationController
  before_filter :require_user, :admin_required
  def show

  end
  
end
