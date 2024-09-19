#!/bin/bash

javac -classpath "../../target/*:../../target/dependency/*" *.java
jar cf "../../target/svgMapToolsBackwardCompatibilityWrapper.jar" *.class