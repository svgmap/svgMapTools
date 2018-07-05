@echo off 
set CP=%CLASSPATH%;.\lib\*;.\shape2svgmap.jar;.
java -Xmx800m -classpath %CP% Shape2WGS84 %*
