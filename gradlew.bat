@ECHO OFF
SETLOCAL

SET APP_HOME=%~dp0
SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

IF NOT "%JAVA_HOME%"=="" GOTO findJavaFromJavaHome
SET JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
IF %ERRORLEVEL% EQU 0 GOTO execute
ECHO.
ECHO ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
EXIT /B 1

:findJavaFromJavaHome
SET JAVA_HOME=%JAVA_HOME:"=%
SET JAVA_EXE=%JAVA_HOME%/bin/java.exe
IF EXIST "%JAVA_EXE%" GOTO execute
ECHO.
ECHO ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
EXIT /B 1

:execute
"%JAVA_EXE%" "-Xmx64m" "-Xms64m" %JAVA_OPTS% %GRADLE_OPTS% ^
  "-Dorg.gradle.appname=gradlew" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

ENDLOCAL
