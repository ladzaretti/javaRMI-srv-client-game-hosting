SET var=%CD%
cd %var%\rmigameclient
javac *.java

cd ..
jar cvf rmigameclient.jar rmigameclient\*.class
pause