#!/bin/bash

#set -e
#CMD=""
#while [[ $# -gt 0 ]]; do
#	case $1 in
#		master)
#			tail -f /dev/null
#			;;
#		server)
#			CMD="${JMETER_HOME}bin/jmeter-server \
#			  -Dserver.rmi.localport=50000 \
#			  -Dserver_port=1099 \
#			  -Jserver.rmi.ssl.disable=true"
#			shift
#			;;
#		*)
#			CMD+=" $1"
#			shift
#			;;
#	esac
#done
#eval "$CMD"

# new setting
CMD="export JVM_ARGS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000 && \
		${JMETER_HOME}bin/jmeter-server \
			  -Dserver.rmi.localport=50000 \
			  -Dserver_port=1099 \
			  -Jserver.rmi.ssl.disable=true"
eval "$CMD"
