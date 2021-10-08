#!/usr/bin/python

import subprocess
import select
import time
import re
import string
import sys
from datetime import datetime

# TITLE|title string
print "TITLE|Show CPU Usage : top -b -d 1"
# SETTINGS|Y Name|X Name|X Range|Y Min value|Min annotation
print "SETTINGS|CPU|TIME|30|0.1|0.5"

def removeCtrlChar(line):
    return filter(string.printable.__contains__, line)

sSaveLine = ""
sPrevTime = 0

def parseLine(line):
    global sSaveLine
    global sPrevTime
    line = re.sub(' +', ' ', line)
    line = line.strip()
    splitLine = line.split(' ')
    count = 0
    if  "top" == splitLine[0] or "MiB" == splitLine[0] or "PID" == splitLine[0]:
        return

    if "%Cpu(s):" == splitLine[0]:
        currTime = int(time.time())

        if sPrevTime == currTime:
            return
        sPrevTime = currTime

        sSaveLine = str(currTime)

        total = 100.0 - float(splitLine[7])
        sSaveLine += "|Total#"
        sSaveLine += str(total)
        return

    if sSaveLine != "" and len(splitLine) >= 12 and splitLine[8] != "0.0":
        saveItem = "|" + splitLine[0] + "-" + splitLine[11] + "#" + splitLine[8]
        sSaveLine += str(saveItem)

while True:
    line = sys.stdin.readline()
    if line == "":
        break
    if "Tasks: " in line:
        if sSaveLine != "":
            print sSaveLine
            sys.stdout.flush()
            sSaveLine = ""
        continue
    parseLine(line)

