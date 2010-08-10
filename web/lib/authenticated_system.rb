module AuthenticatedSystem
  protected
    # Returns true or false if the person is logged in.
    # Preloads @current_person with the person model if they're logged in.
    def logged_in?
      current_user != :false
    end

    def current_user_session
      return @current_user_session if defined?(@current_user_session)
      @current_user_session = UserSession.find
    end

    def current_user
      return @current_user if defined?(@current_user)
      @current_user = current_user_session && current_user_session.record
    end
       
    # Inclusion hook to make #current_user and #logged_in?
    # available as ActionView helper methods.
    def self.included(base)
      base.send :helper_method, :current_user, :logged_in?
    end

    def admin_required
      unless current_person and current_person.attributes['is_admin']
        flash[:error] = "Admin access required"
        redirect_to welcome_url
      end
    end

    def require_user
      unless current_user
        store_location
        flash[:notice] = "You must be logged in to access this page"
        redirect_to new_user_session_url
        return false
      end
    end

    def admin_required
      unless current_user and current_user.attributes['is_admin']
        flash[:error] = "Admin access required"
        redirect_to welcome_url
      end
    end

    def admin?
      current_user.attributes[:is_admin]
    end

    def require_no_user
      if current_user
        store_location
        flash[:notice] = "You must be logged out to access this page"
        redirect_to new_user_url
        return false
      end
    end

end
