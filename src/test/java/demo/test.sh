#!/bin/bash

# pressure test, before we run, make sure using Utilities.testMain
# to generate the msg.txt, which are basically requests
# and do c times request


ARG1=${1:-1}
ARG2=${2:-0}

for (( a=0; a<1;a++ ))
do
  for (( c=$ARG2; c<$ARG1;c++ ))
  do
     nc localhost 12345 < "./txt/msg$c.txt" > "./txt/out$c.txt" &
#    nc localhost 12345 < "./txt/msg$c.txt" > /dev/null &
  done
done

echo "nc localhost 12345 ./txt/msg[$ARG2-$ARG1].txt done"
wait
echo "finish all sending command"
