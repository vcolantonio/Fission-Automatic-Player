#!/usr/bin/bash

if [ $# -lt 2 ]
then
	echo "Usage: $0 <IP> <PORT>"
	exit 1
fi

java -jar Player_EUREKA.jar $1 $2
