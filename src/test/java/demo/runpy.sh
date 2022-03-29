#!/usr/bin/env python3

def compare(msgFile, outFile):
  file1  = open(msgFile, 'r')
  file2  = open(outFile, 'r')
  lines1  = file1.readlines()
  lines2  = file2.readlines()

  # Strips the newline character
  for line in lines1:

      print(line.strip())

def main():
  for i in range(0, 20):
    msg = './txt/msg' + str(i) + '.txt'
    out = './txt/out' + str(i) + '.txt'

    compare(msg, out)

if __name__ == '__main__':
  main()