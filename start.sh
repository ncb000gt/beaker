#!/usr/bin/env bash

SCALA=`which scala`

L=libs
cp=`echo $L/*.jar|sed 's/ /:/g'`

echo "$SCALA -classpath $cp server"
$SCALA -cp "$cp:./build/" server