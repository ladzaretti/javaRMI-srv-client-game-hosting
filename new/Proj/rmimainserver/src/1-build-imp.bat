SET var=%CD%
cd %var%\rmimainserver
javac *.java

cd ..
jar cvf rmimainserver.jar rmimainserver\*.class
pause