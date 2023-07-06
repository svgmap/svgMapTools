#!/bin/bash
java -Xmx800m -classpath "../target/dependency/*:../target/svgMapTools-202305.jar:" org.svgmap.shape2svgmap.MainWrapper HyperBuilder $@
