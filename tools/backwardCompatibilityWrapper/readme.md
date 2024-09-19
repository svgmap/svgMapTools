このディレクトリのコードは、maven以前(202305より前)のパッケージとのプログラムの起動方法の互換性を提供するラッパーです。

./make.sh

java -classpath target/svgMapTools.jar:target/svgMapToolsBackwardCompatibilityWrapper.jar:target/dependency/* Shape2SVGMap
java -classpath target/svgMapTools.jar:target/svgMapToolsBackwardCompatibilityWrapper.jar:target/dependency/* Shape2ImageSVGMap
java -classpath target/svgMapTools.jar:target/svgMapToolsBackwardCompatibilityWrapper.jar:target/dependency/* Shape2WGS84
java -classpath target/svgMapTools.jar:target/svgMapToolsBackwardCompatibilityWrapper.jar:target/dependency/* HyperBuilder