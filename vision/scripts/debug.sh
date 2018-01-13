#!/bin/bash

screen -dmS vision2018 java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar vision.jar