#!/bin/sh
ENABLE="true"
 
if [ "$ENABLE" == "true" ]; then
echo running in 3 secounds
sleep 0
while [ "$ENABLE" == "true" ]
do
 
clear
rm /usr/local/nginx/html/computerInfo.txt
top -bn1 | grep "Cpu(s)" | \
           sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | \
           awk '{print 100 - $1""}' >> /usr/local/nginx/html/computerInfo.txt
free -m >> /usr/local/nginx/html/computerInfo.txt
vnstat >> /usr/local/nginx/html/computerInfo.txt
 
 
 
sleep 5
done
else
 
echo not enabled
 
fi
