# Methods added to this helper will be available to all templates in the application.
module ApplicationHelper

  ## Menu helpers
  
  def menu
    #home = menu_element("Welcome",   welcome_path)

    dicts = [];
    Dictionary.find_each do |dict|
      dicts << menu_element(dict.name, dictionary_path(dict))
    end
    
    dictionary = master_menu_element("Dictionary", dictionaries_path, dicts)
    
    links = [dictionary]

    my_suggestions = []


    if logged_in? and current_user
      my_suggestions << menu_element("Exam", new_exam_path)
      my_suggestions << menu_element("Study", new_study_session_path)
      my_suggestions << menu_element("Suggestions", suggestions_path)

      my_spellbook = master_menu_element("Instruments", suggestions_path, my_suggestions)
      links << my_spellbook
    end
    


    if admin?
      admins = []
      admins << menu_element("Users", admin_users_path)
      admins << menu_element("Suggestions", admin_suggestions_path)
      admin_item = master_menu_element("Admin", admin_users_path, admins)
      links << admin_item
    end
    
    links
  end
  def menu_element(content, address)
   { :content => content, :href => address }
  end

  def master_menu_element(content, address, submenu)
   { :content => content, :href => address, :child => submenu }
  end

  def menu_link_to(link, options = {})
    link_to(link[:content], link[:href], options)
  end

  def menu_li(link, options = {})
    #klass = "n-#{link[:content].downcase}"
    klass = "active" if current_page?(link[:href])

    if link[:child]
      list_items = content_tag(:ul, submenu_li(link[:child]), :class => "sub")
      klass = "active" if link[:child].count { |l| current_page?l[:href] } > 0
      content_tag(:li, menu_link_to(link, options) + list_items, :class => klass)
    else      
      content_tag(:li, menu_link_to(link, options), :class => klass)
    end
    
  end

  def submenu_li(links, options = {})
    markup = "\n"

    for link in links 
      #klass = "n-#{link[:content].downcase}"      
      klass = "active" if current_page?(link[:href])

      markup << content_tag(:li, menu_link_to(link, options), :class => klass)
      markup << "\n"
    end

    markup
    
  end

  def render_flash
    render :partial => 'shared/flash', :locals => { :flash => flash }
  end

  # Display a flash message in RJS templates Usage: page.notify :warning, "This
  # is the message", 5.0 Puts the message into a flash of type 'warning', fades
  # over 5 secs
  def notify(type, message, fade_duration)
    type = type.to_s  # symbol to string
    page.replace 'flash', "<h4 id='flash' class='alert #{type}'>#{message}</h4>"
    page.visual_effect :fade, 'flash', :duration => fade_duration
  end

  def admin?
    logged_in? and current_user and current_user.attributes['is_admin']
  end

    def format_translation(translation)
    new_translation = ""

    pattern_transcript = /\[[\S]+\]/
    pos = pattern_transcript =~ translation
    transcription = ""

    if pos
      transcription = $~.to_s
      translation[pos..transcription.length] = ""
      new_translation = "\<b\>" + transcription + "\</b\>" + "\<br/\>" + "\<br/\>"
    end

    rows = translation.split("\n")

    rows.each_with_index do |row, index|
      new_translation += row
      new_translation += "\<br/\>" + "\<br/\>" unless index == rows.count - 1 or index == 0
    end

    new_translation
  end
end
