#!/bin/bash

javac -classpath "../../target/svgMapTools-202305.jar:../../target/dependency/*" *.java
jar cf "../../target/svgMapToolsBackwardCompatibilityWrapper.jar" *.class