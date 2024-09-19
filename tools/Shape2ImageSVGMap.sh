#!/bin/bash
java -Xmx800m -classpath "../target/*:../target/dependency/*" org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap $@