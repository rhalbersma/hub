#!/bin/sh

# javac -O -Xlint:all hub/Hub.java
javac -O -Xlint:all */*.java
jar cfm hub.jar Manifest */*.class # go_stone.wav

# mv hub.jar ..

