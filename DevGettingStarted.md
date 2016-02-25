# Introduction #

To develop Spellbook you'll need a couple of tools - Java 6 SE JDK, Maven 2, NetBeans IDE 6.8+.
You may eventually need a Google account as well(in case you happen to join the official Spellbook team)

# Obtaining the necessary software #

  * Download Java 6 SE JDK from here
  * Download NetBeans 6.8 from here
  * Download Maven 2 from here

# Building and running Spellbook #

> ## Import the project code from Subversion ##
  * Start NetBeans
  * Go to Team->Subversion->Checkout
  * Enter http://spellbook-dictionary.googlecode.com/svn/trunk/ as the repo url(there is a different url for project members). Leave the username and password fields empty. Press "next".
  * On the next page select "skip trunk and checkout only its contents". Select some appropriate checkout path - I personally use $HOME/projects/spellbook, where $HOME is my home folder. Now press "Finish" and wait I bit while the contents of the repository get downloaded to your computer.
  * At the end of the process you'll get an dialog asking you if you'd like to open the freshly checked-out project - press the "open project" button.
  * From the next dialog expand the Spellbook dictionary project, select it and all of the project module beneath it and press "open".
> ## Prepare the project ##
  * If you've properly completed the instructions in the previous section you're now seeing five items in your projects tab at the left of your NetBeans IDE.
  * Right click on Spellbook Dictionary UI and select "Build with dependencies"
  * While still on that module select "Run" from the menu bar or press F6. Spellbook should be running in a moment in front of you.
  * During it's first run Spellbook will likely ask you for its database - point it to the directory where you've decompressed the database available on the official project site.

> ## Hacking time ##
  * Improve
  * Debug
  * Commit
  * Repeat above steps
