#!/bin/sh
#  ======================================================================
#
#  This is the main entry point for the build system.
#
#  Users should be sure to execute this file rather than 'ant' to ensure
#  the correct version is being used with the correct configuration.
#
#  ======================================================================
#

# ******************************************************
# Ignore the ANT_HOME variable: we want to use *our*
# ANT version and associated JARs.
# ******************************************************
# Ignore the users classpath, cause it might mess
# things up
# ******************************************************

unset CLASSPATH
unset ANT_HOME
JAXP_DOM_FACTORY=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl
JAXP_SAX_FACTORY=org.apache.xerces.jaxp.SAXParserFactoryImpl

ANT_OPTS="-Djava.protocol.handler.pkgs=org.jboss.net.protocol -Djavax.xml.parsers.DocumentBuilderFactory=$JAXP_DOM_FACTORY -Djavax.xml.parsers.SAXParserFactory=$JAXP_SAX_FACTORY -Dbuild.script=build.sh"


if [ -z $JAVA_HOME ]; then
	echo "\$JAVA_HOME not defined"
	exit
fi

if [ ! -f "$JAVA_HOME/bin/javac" ]; then
	echo "No java compiler found"
	exit
fi

JAVACMD=$JAVA_HOME/bin/java
ANT_CMD_LINE_ARGS=$@
JAVA_PARAM=

# Required libraries for running ant

ROOT=..
CUR_PROJECT=.

BIN=$CUR_PROJECT
LIBRARIES=$ROOT/lib
ANT_LIB=$LIBRARIES/ant
BCEL_LIB=$LIBRARIES/bcel
LOG4J_LIB=$LIBRARIES/log4j
BUILD_XML=$CUR_PROJECT/build.xml

# set the local classpath
LOCALCLASSPATH=$CLASSPATH
for i in $ANT_LIB/*.jar; do
	LOCALCLASSPATH=$i:$LOCALCLASSPATH
done

for i in $BCEL_LIB/*.jar; do
	LOCALCLASSPATH=$i:$LOCALCLASSPATH
done

for i in $LOG4J_LIB/*.jar; do
	LOCALCLASSPATH=$i:$LOCALCLASSPATH
done

# for modern compiler
if [ -f "$JAVA_HOME/lib/tools.jar" ]; then
	LOCALCLASSPATH=$LOCALCLASSPATH:$JAVA_HOME/lib/tools.jar	
fi

echo "Run ant"
$JAVACMD $JAVA_PARAM -classpath $LOCALCLASSPATH -Dant.home="$ANT_HOME" $ANT_OPTS org.apache.tools.ant.Main -buildfile $BUILD_XML $ANT_CMD_LINE_ARGS
