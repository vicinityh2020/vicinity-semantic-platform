#!/bin/bash

JAR=semantic-repository-0.6.3.2.jar

SERVER_PORT=9004
GRAPHDB_ENDPOINT=http://localhost:7200/repositories/vicinity-test

JSONLD_SCHEMA_LOCATION=file:///home/kostelni/work/eu-projekty/vicinity/github-workspace/vicinity-semantic-platform/semantic-repository/bin/config/json-ld/thing.jsonld
#JSONLD_SCHEMA_LOCATION=file:///home/peter/semantic-platform/semantic-repository/config/json-ld/thing.jsonld

MAIN_CLASS=sk.intersoft.vicinity.platform.semantic.service.SemanticRepositoryServer

# LOG CONFIGURATION ..
LOGS_FOLDER=logs
DEFAULT_LOG=$LOGS_FOLDER/repository.log

LOG_CONFIG=config/logging

LOGGING_CONFIG_SOURCE=${LOG_CONFIG}/logging.properties
LOGBACK_CONFIG_SOURCE=${LOG_CONFIG}/logback.xml

LOGGING_CONFIG=${LOG_CONFIG}/resolved.logging.properties
LOGBACK_CONFIG=${LOG_CONFIG}/resolved.logback.xml



COMMAND=$1

PID=$(ps -eaf | grep $MAIN_CLASS | grep server.port=$SERVER_PORT | grep -v grep | awk '{print $2}')

echo "command: $COMMAND"
echo "pid: $PID"

if [[ $COMMAND ==  "stop" ]]; then
  echo "stopping semantic repository"

    if [[ "" !=  "$PID" ]]; then
      echo "killing: $PID"
      kill -15 $PID
    else
      echo "process not found"
    fi


else
  echo "starting semantic repository"

    if [[ "" !=  "$PID" ]]; then
      echo "semantic repository is running"
    else
        rm $DEFAULT_LOG;

        echo "prepare logging";
        java -cp "lib/*" \
            -Dlogback.configurationFile=$LOGBACK_CONFIG_SOURCE \
            -Djava.util.logging.config.file=$LOGGING_CONFIG_SOURCE \
            -Dlogs.folder=$LOGS_FOLDER \
            sk.intersoft.vicinity.platform.semantic.config.PrepareLogging > $DEFAULT_LOG;

        echo "start service";
        nohup java -cp "lib/*" \
            -Dserver.port=$SERVER_PORT \
            -Dgraphdb.endpoint=$GRAPHDB_ENDPOINT \
            -Djsonld.schema.location=$JSONLD_SCHEMA_LOCATION \
            -Dlogback.configurationFile=$LOGBACK_CONFIG \
            -Djava.util.logging.config.file=$LOGGING_CONFIG \
            $MAIN_CLASS >> $DEFAULT_LOG 2>&1 &
        echo "semantic repository started"

    fi


fi



