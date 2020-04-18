#!/bin/bash

NAME=griffin-$(git tag -l | tail -n 1).zip

#build project
./gradlew build
./gradlew nativeImage -P com.palantir.graal.cache.dir=/tmp

cp build/graal/griffin release/

#zip the contents of release directory
cd release && zip -r $NAME .
cd .. && mv release/$NAME .

#remove the copied jar from release
#rm release/griffin
rm release/griffin
