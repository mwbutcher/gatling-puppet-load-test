#!/bin/sh

SIM_ID=$1
MASTER=$2
SCENARIO=$3
SBTPATH=$4

export PUPPET_GATLING_SIMULATION_ID=$SIM_ID
export PUPPET_GATLING_MASTER_BASE_URL=https://$MASTER:8140
export PUPPET_GATLING_SIMULATION_CONFIG=./config/scenarios/$SCENARIO

cd ../simulation-runner
java -Xms512M -Xmx1536M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M -Xss2M -Dsbt.log.noformat=true -jar $SBTPATH run
