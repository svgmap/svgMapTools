@echo off 
set CP=%CLASSPATH%;..\target\dependency\*;..\target\svgMapTools-202307.jar org.svgmap.shape2svgmap.MainWrapper
java -Xmx800m -classpath %CP% Shape2SVGMap %*
