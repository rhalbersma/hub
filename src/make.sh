#!/bin/sh

javac -O hub/Hub.java
jar cfm hub.jar Manifest */*.class

