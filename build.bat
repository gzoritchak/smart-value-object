@echo off

REM  ======================================================================
REM
REM  This is the main entry point for the build system.
REM
REM  Users should be sure to execute this file rather than 'ant' to ensure
REM  the correct version is being used with the correct configuration.
REM
REM  ======================================================================
REM

REM ******************************************************
REM Ignore the ANT_HOME variable: we want to use *our*
REM ANT version and associated JARs.
REM ******************************************************
REM Ignore the users classpath, cause it might mess
REM things up
REM ******************************************************

REM Init env

@setlocal

set CLASSPATH=
set ANT_HOME=
REM set JAXP_DOM_FACTORY=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
REM set JAXP_SAX_FACTORY=org.apache.crimson.jaxp.SAXParserFactoryImpl
set JAXP_DOM_FACTORY=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl
set JAXP_SAX_FACTORY=org.apache.xerces.jaxp.SAXParserFactoryImpl

set ANT_OPTS=-Djava.protocol.handler.pkgs=org.jboss.net.protocol -Djavax.xml.parsers.DocumentBuilderFactory=%JAXP_DOM_FACTORY% -Djavax.xml.parsers.SAXParserFactory=%JAXP_SAX_FACTORY% -Dbuild.script=build.bat

REM ******************************************************
REM - "for" loops have been unrolled for compatibility
REM   with some WIN32 systems.
REM ******************************************************


:chekJavaHome
if "%JAVA_HOME%"=="" goto java_home_not_def

:checkJavaCompiler
if not exist "%JAVA_HOME%\bin\java.exe" goto no_java_compiler

set JAVACMD=%JAVA_HOME%\bin\java
set ANT_CMD_LINE_ARGS=%*
set JAVA_PARAM=

REM Required libraries for running ant



set ROOT=..
set CUR_PROJECT=.

set BIN=%CUR_PROJECT%
set LIBRARIES=%ROOT%\lib
set ANT_LIB=%LIBRARIES%\ant
set BCEL_LIB=%LIBRARIES%\bcel
set LOG4J_LIB=%LIBRARIES%\log4j
set BUILD_XML=%CUR_PROJECT%\build.xml


REM set the local classpath
set LOCALCLASSPATH=%CLASSPATH%
for %%i in ("%ANT_LIB%\*.jar") do call "%BIN%\lcp.bat" "%%i"
for %%i in ("%BCEL_LIB%\*.jar") do call "%BIN%\lcp.bat" "%%i"
for %%i in ("%LOG4J_LIB%\*.jar") do call "%BIN%\lcp.bat" "%%i"

REM for modern compiler
if exist "%JAVA_HOME%\lib\tools.jar" call "%BIN%\lcp.bat" "%JAVA_HOME%\lib\tools.jar"

:runAnt
echo Run ant
%JAVACMD% %JAVA_PARAM% -classpath %LOCALCLASSPATH% -Dant.home="%ANT_HOME%" %ANT_OPTS% org.apache.tools.ant.Main -buildfile %BUILD_XML% %ANT_CMD_LINE_ARGS%
goto end


:java_home_not_def
echo JAVA_HOME is not set ant java could not be located.
echo Please set JAVA_HOME
goto end

:no_java_compiler
echo JAVA compiler could not be located.
echo Please check your JAVA_HOME

:end
set LOCALCLASSPATH=

@endlocal
