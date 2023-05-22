rm -rf "../src_cpy"
cp -r "../src" "../src_cpy"
cp -r "../src_cpy/main/resources/META-INF/" "../src_cpy/main/java"

cd "../src_cpy/main/java"

CP="../../../target/dependency/*:."

javac -encoding utf-8 -classpath "$CP" "org/svgmap/shape2svgmap/svgMapMarkFactory.java"
javac -encoding utf-8 -classpath "$CP" "org/svgmap/shape2svgmap/MainWrapper.java"

jar cf "../../../target/svgMapTools-202305.jar" META-INF/services/* org/svgmap/shape2svgmap/*.class org/svgmap/shape2svgmap/cds/*.class

cd "../../../tools"