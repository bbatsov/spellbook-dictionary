#!/bin/sh

SPELLBOOK_VERSION=0.3-SNAPSHOT

if [ -e /opt/spellbook/dictionary-ui-0.3-SNAPSHOT-jar-with-dependencies.jar ]; then
    java -jar /opt/spellbook/dictionary-ui-0.3-SNAPSHOT-jar-with-dependencies.jar
else
    # find the script dir
    script_dir=`dirname "$0"`
    # go to the script dir
    cd "$script_dir"

    # run Spellbook's binary
    java -jar dictionary-ui-0.3-SNAPSHOT-jar-with-dependencies.jar
fi
