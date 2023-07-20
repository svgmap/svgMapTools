cd ..
rmdir /s /q src_cpy

xcopy /e src src_cpy\

xcopy /e src_cpy\main\resources src_cpy\main\java


cd src_cpy\main\java

set CP=%CLASSPATH%;..\..\..\target\dependency\*;.

javac -encoding utf-8 -classpath %CP% org\svgmap\shape2svgmap\svgMapMarkFactory.java
javac -encoding utf-8 -classpath %CP% org\svgmap\shape2svgmap\MainWrapper.java

jar cf ..\..\..\target\svgMapTools-202307.jar META-INF\services\* org\svgmap\shape2svgmap\*.class org\svgmap\shape2svgmap\cds\*.class

cd ..\..\..\tools