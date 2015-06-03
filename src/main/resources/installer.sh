#!/bin/bash

JMETER_HOME=$1
HOME=`pwd`
MQTT_JAR=mqtt-jmeter.jar

if [ "$JMETER_HOME" = "" ] ; then
    echo "usage: $0 path_to_jmeter_2_3_1"
    echo "abort."
    exit 1
fi

if [ ! -d $JMETER_HOME ] ; then
    echo "directory \"$JMETER_HOME\" not exists."
    echo "abort."
    exit 1
fi

if [ ! -e $MQTT_JAR ] ; then
    echo "distribution file \"$MQTT_JAR\" not exists."
    echo "abort."
    exit 1
fi

JMETER_JAR=ApacheJMeter_core.jar

if [ ! -e $JMETER_HOME/lib/ext/$JMETER_JAR ] ; then
    echo "file \"$JMETER_JAR\" not exists."
    echo "abort."
    exit 1
fi

cd $JMETER_HOME/lib/ext

echo "copy mqtt-jmeterd jars.."
cp $HOME/$MQTT_JAR .

JMETER_JAR_BACKUP=$JMETER_JAR.backup
if [ ! -e $JMETER_JAR_BACKUP ] ; then
    echo "create backup of \"$JMETER_JAR\".."
    cp $JMETER_JAR $JMETER_JAR_BACKUP
else
    echo "backup of \"$JMETER_JAR\" exist, don't need to create.."
fi

MESSAGES=org/apache/jmeter/resources/messages.properties
MESSAGES_TMP=$MESSAGES.tmp

echo "exctract \"$MESSAGES\".."
jar -xfv $JMETER_JAR $MESSAGES

mv $MESSAGES $MESSAGES_TMP

echo "convert to unix format.."
dos2unix $MESSAGES_TMP

echo "add values for mqtt-jmeter to \"messages.properties\".."
cat $HOME/messages.properties >> $MESSAGES_TMP

echo "sort \"messages.properties\".."
sort $MESSAGES_TMP > $MESSAGES

echo "save \"messages.properties\" to \"$JMETER_JAR\".."
jar -uvf $JMETER_JAR $MESSAGES

echo "remove work files.."
rm -r org/

echo "done."
cd $HOME