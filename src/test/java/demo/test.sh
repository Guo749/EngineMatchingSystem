#!/bin/bash


for (( c=0; c<5;c++ ))
do
  nc localhost 12345 < "msg$c.txt" &
done

wait