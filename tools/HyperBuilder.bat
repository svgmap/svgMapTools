@echo off 
set CP=%CLASSPATH%;..\target\dependency\*;..\target\svgMapTools-202305.jar org.svgmap.shape2svgmap.MainWrapper
java -classpath %CP% HyperBuilder %*
