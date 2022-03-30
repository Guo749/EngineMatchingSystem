#!/bin/bash

# pressure test, before we run, make sure using Utilities.testMain
# to generate the msg.txt, which are basically requests
# and do c times request

for (( c=0; c<$1;c++ ))
  do
     nc localhost 12345 < "./txt/msg$c.txt" > "./txt/out$c.txt" &

     # if not want output, just uncomment this one
   # nc localhost 12345 < "./txt/msg$c.txt" > /dev/null &

  done

echo "nc localhost 12345 ./txt/msg[0-$c].txt done"
wait
echo "finish all sending command"