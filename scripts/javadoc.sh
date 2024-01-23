#!/bin/bash

pathToImplementor="../java-solutions/info/kgeorgiy/ja/okorochkova/implementor"
moduleImplementor="../java-advanced-2023/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor"

javadoc -author -private -d javadoc \
  $pathToImplementor/{Implementor.java,CodeConstructor.java} \
  $moduleImplementor/{ImplerException.java,Impler.java,JarImpler.java}
