#!/usr/bin/env bash

SCALA=`which scala`

L=libs
cp=`echo $L/*.jar|sed 's/ /:/g'`

$SCALA -cp "$cp:./build/" com.digitaltumbleweed.server.server