cd ..
rmdir /s /q src_cpy

xcopy /e src src_cpy\

cd src_cpy\main\java

set CP=%CLASSPATH%;..\..\..\target\dependency\*;.

javac -encoding utf-8 -classpath %CP% org\svgmap\shape2svgmap\svgMapMarkFactory.java
javac -encoding utf-8 -classpath %CP% org\svgmap\shape2svgmap\MainWrapper.java

jar cf ..\..\..\target\svgMapTools-202305.jar ..\resources\META-INF\services\* org\svgmap\shape2svgmap\*.class org\svgmap\shape2svgmap\cds\*.class

cd ..\..\..\tools