#!/usr/bin/env bash
#  /etc/init.d/easy-sword2
# chkconfig: 2345 92 58

### BEGIN INIT INFO
# Provides:          easy-sword2
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Short-Description: Starts the easy-sword2 service
# Description:       This file is used to start the daemon
#                    and should be placed in /etc/init.d
### END INIT INFO

NAME="easy-sword2"
EXEC="/usr/bin/jsvc"
APPHOME="/usr/local/easy-sword2"
JAVA_HOME="/usr/lib/jvm/jre"
CLASSPATH="$APPHOME/bin/$NAME.jar:`echo $APPHOME/lib/*.jar | sed 's/ /:/g'`"
CLASS="nl.knaw.dans.easy.sword2.ServiceStarter"
ARGS=""
USER="easy-sword2"
PID="/var/run/$NAME.pid"
OUTFILE="/var/log/$NAME/$NAME.out"
ERRFILE="/var/log/$NAME/$NAME.err"
WAIT_TIME=60

jsvc_exec()
{
    cd ${APPHOME}
    # Set LC_ALL to a locale with UTF-8 to make sure non-ASCII file names are written correctly to the file system (see: EASY-1254).
    LC_ALL=en_US.UTF-8 \
    ${EXEC} -home ${JAVA_HOME} -cp ${CLASSPATH} -user ${USER} -outfile ${OUTFILE} -errfile ${ERRFILE} -pidfile ${PID} -wait ${WAIT_TIME} \
          -Dapp.home=${APPHOME} -Dconfig.file=${APPHOME}/cfg/application.conf \
          -Dlogback.configurationFile=${APPHOME}/cfg/logback-service.xml $1 ${CLASS} ${ARGS}
}

start_jsvc_exec()
{
    jsvc_exec
    if [[ $? == 0 ]]; then # start is successful
        echo "$NAME has started."
    else
        echo "$NAME did not start successfully (exit code: $?)."
    fi
}

stop_jsvc_exec()
{
    jsvc_exec "-stop"
    if [[ $? == 0 ]]; then # stop is successful
        echo "$NAME has stopped."
    else
        echo "$NAME did not stop successfully (exit code: $?)".
    fi
}

restart_jsvc_exec()
{
    echo "Restarting $NAME ..."
    jsvc_exec "-stop"
    if [[ $? == 0 ]]; then # stop is successful
        echo "$NAME has stopped, starting again ..."
        jsvc_exec
        if [[ $? == 0 ]]; then # start is successful
            echo "$NAME has restarted."
        else
            echo "$NAME did not start successfully (exit code: $?)."
        fi
    else
        echo "$NAME did not stop successfully (exit code: $?)."
    fi
}

case "$1" in
    start)
        if [ -f "$PID" ]; then # service is running
            echo "$NAME is already running, no action taken."
            exit 1
        else
            echo "Starting $NAME ..."
            start_jsvc_exec
        fi
    ;;
    stop)
        if [ -f "$PID" ]; then # service is running
            echo "Stopping $NAME ..."
            stop_jsvc_exec
        else
            echo "$NAME is not running, no action taken."
            exit 1
        fi
    ;;
    restart)
        if [ -f "$PID" ]; then # service is running
            restart_jsvc_exec
        else
            echo "$NAME is not running, just starting ..."
            start_jsvc_exec
        fi
    ;;
    status)
        if [ -f "$PID" ]; then # if service is running
            echo "$NAME (pid `cat $PID`) is running."
        else
            echo "$NAME is stopped."
        fi
    ;;
    *)
        echo "Usage: sudo service $NAME {start|stop|restart|status}" >&2
        exit 3
    ;;
esac
