#!/bin/bash

BUILD=build
JAR=semantic-repository-1.0-SNAPSHOT.jar

rm -rf $BUILD
mkdir $BUILD
mkdir $BUILD/lib

echo "build folder created"

cp -a ../target/dependency/* $BUILD/lib/
echo "added dependencies"


cp -a ../target/$JAR $BUILD/lib/
echo "added jar"


cp -a ../bin/semantic-repository.sh $BUILD/
echo "added script"

zip -r build/semantic-repository.zip build/.
echo "zipped"
