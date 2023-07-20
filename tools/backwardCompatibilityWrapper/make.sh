#!/bin/bash

javac -classpath "../../target/svgMapTools-202307.jar:../../target/dependency/*" *.java
jar cf "../../target/svgMapToolsBackwardCompatibilityWrapper.jar" *.class