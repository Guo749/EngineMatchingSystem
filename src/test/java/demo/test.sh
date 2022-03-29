#!/bin/bash

# pressure test, before we run, make sure using Utilities.testMain
# to generate the msg.txt, which are basically requests
# and do c times request
# todo: right now we still need to hand-click some, maybe automate

for (( c=0; c<20;c++ ))
do
  nc localhost 12345 < "./txt/msg$c.txt" > "./txt/out$c.txt" &
done

wait