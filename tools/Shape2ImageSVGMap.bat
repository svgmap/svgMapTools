@echo off 
set CP=%CLASSPATH%;.\lib\*;.\shape2svgmap.jar;.
java -classpath %CP% Shape2ImageSVGMap %*
