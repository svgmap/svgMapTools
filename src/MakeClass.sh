CLASSPATH="../tools/lib/*":.

rm *.class
rm ./org/svgmap/shape2svgmap/*.class
rm ./org/svgmap/shape2svgmap/cds/*.class

export CLASSPATH

javac -encoding sjis org/svgmap/shape2svgmap/svgMapMarkFactory.java
javac -encoding sjis HyperBuilder.java
javac -encoding sjis Shape2ImageSVGMap.java
javac -encoding sjis Shape2SVGMap.java
javac -encoding sjis Shape2WGS84.java

jar cfm ../tools/shape2svgmap.jar SvgMapTools.mf META-INF/services/* org/svgmap/shape2svgmap/*.class org/svgmap/shape2svgmap/cds/*.class HyperBuilder.class Shape2ImageSVGMap.class Shape2SVGMap.class Shape2WGS84.class
