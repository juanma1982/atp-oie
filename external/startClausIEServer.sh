#!/bin/bash
cd external
#DIR=`dirname $0`
#echo $DIR
echo $*
java -Xms512m -Xmx3072m -jar clausieServer.jar

