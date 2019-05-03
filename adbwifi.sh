#!/bin/bash
adb kill-server
adb tcpip 5555
sleep 5
export phone_ip=$(adb shell "ip addr show wlan0 | grep -e wlan0$ | cut -d\" \" -f 6 | cut -d/ -f 1")
adb connect "${phone_ip}:5555"
