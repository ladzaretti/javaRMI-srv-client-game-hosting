SET var=%CD%
cd %var%\rmigameserver
javac *.java

cd ..
jar cvf rmigameserver.jar rmigameserver\*.class
pause