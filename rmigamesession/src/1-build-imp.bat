SET var=%CD%
cd %var%\rmigamesession
javac *.java

cd ..
jar cvf rmigamesession.jar rmigamesession\*.class
pause