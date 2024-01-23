#!/bin/bash

pathToImplementor="../java-solutions/info/kgeorgiy/ja/okorochkova/implementor"
implementorTest="../java-advanced-2023/artifacts/info.kgeorgiy.java.advanced.implementor.jar"

javac -cp $implementorTest $pathToImplementor/*.java -d .

jar -cfm Implementor.jar MANIFEST.MF info

rm -rf info