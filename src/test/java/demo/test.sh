#!/bin/bash

# pressure test, before we run, make sure using Utilities.testMain
# to generate the msg.txt, which are basically requests
# and do c times request
# todo: right now we still need to hand-click some, maybe automate

for (( a=0; a<1;a++ ))
do
  for (( c=0; c<$1;c++ ))
  do
     nc localhost 12345 < "./txt/msg$c.txt" > "./txt/out$c.txt" &
#    nc localhost 12345 < "./txt/msg$c.txt" > /dev/null &
    echo "loop $a-$c"
  done
done

wait
echo "done"