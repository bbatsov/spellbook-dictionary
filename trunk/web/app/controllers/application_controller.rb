# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  include AuthenticatedSystem
  helper :all
  helper_method :current_user_session
  filter_parameter_logging :password, :password_confirmation
  
  private
  
    def store_location
      session[:return_to] = request.request_uri
    end
    
    def redirect_back_or_default(default)
      redirect_to(session[:return_to] || default)
      session[:return_to] = nil
    end

    def notify(type, message)
      flash[type] = message
      #logger.error("ERROR: #{message}") if type == :error
    end
end
