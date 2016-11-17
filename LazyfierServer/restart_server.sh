#!/bin/sh

pkill LazyfierServer
sleep 1
BASEDIR=$(dirname "$0")
$BASEDIR/LazyfierServer



