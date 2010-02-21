#!/bin/sh

SPELLBOOK_VERSION=0.3-SNAPSHOT

# find the script dir
script_dir=`dirname "$0"`
# go to the script dir
cd "$script_dir"

# run Spellbook's binary
java -jar spellbook-dictionary-$SPELLBOOK_VERSION.jar
