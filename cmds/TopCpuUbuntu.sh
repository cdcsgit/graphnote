#! /bin/sh

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
/usr/bin/top -b -d 1 | $SCRIPTPATH/TopCpuUbuntuInternal.py

