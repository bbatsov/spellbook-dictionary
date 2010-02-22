#!/bin/sh

DEFAULT_INSTALLATION_FOLDER=/opt/spellbook
SPELLBOOK_VERSION=0.3-SNAPSHOT
SPELLBOOK_JAR=dictionary-ui-$SPELLBOOK_VERSION-jar-with-dependencies.jar

# first we check if spellbook is the default intallation location
if [ -e $DEFAULT_INSTALLATION_FOLDER/$SPELLBOOK_JAR ]; then
    java -jar $DEFAULT_INSTALLATION_FOLDER/$SPELLBOOK_JAR
else
    # otherwise the simple archive was probably used
    # find the script dir
    script_dir=`dirname "$0"`
    # go to the script dir
    cd "$script_dir"

    # run Spellbook's binary
    java -jar $SPELLBOOK_JAR
fi
