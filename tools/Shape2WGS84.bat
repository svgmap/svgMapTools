@echo off 
set CP=%CLASSPATH%;..\target\dependency\*;..\target\* org.svgmap.shape2svgmap.MainWrapper
java -Xmx800m -classpath %CP% Shape2WGS84 %*
