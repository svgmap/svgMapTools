
set CP=%CLASSPATH%;..\tools\lib\*;.

del org\svgmap\shape2svgmap\*.class
del org\svgmap\shape2svgmap\cds\*.class
del *.class

javac -classpath %CP% org\svgmap\shape2svgmap\svgMapMarkFactory.java
javac -classpath %CP% HyperBuilder.java
javac -classpath %CP% Shape2ImageSVGMap.java
javac -classpath %CP% Shape2SVGMap.java
javac -classpath %CP% Shape2WGS84.java
REM javac -source 1.7 -target 1.7 SvgMapTools.java

jar cfm ..\tools\shape2svgmap.jar SvgMapTools.mf META-INF\services\* org\svgmap\shape2svgmap\*.class org\svgmap\shape2svgmap\cds\*.class HyperBuilder.class Shape2ImageSVGMap.class Shape2SVGMap.class Shape2WGS84.class
