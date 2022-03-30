#!/usr/bin/env python3
import socket
import sys
import os
from random import randint
import time

ACCOUNT_LEN=20
WORK_LOAD=30

class Node:
    def __init__(self, account, share):
        self.account = account
        self.share   = share

"""
Used to generate 20 digits random number account
leading zero is ALLOWED
"""
def geneRandAccNum():
    res = ""
    for i in range(0, ACCOUNT_LEN):
        res += str(randint(0, 9))
    return res

"""
Used to generate create clause based on account & balance
"""
def geneCreateAccountClause(account, balance):
    res = "<account id=\"" + account + "\" balance=\"" + balance + "\"/>\n"
    return res

"""
generate  create symbol clause
"""
def geneCreateSymClause(symbol, list):
    res = "<symbol sym=\"" + symbol + "\">\n"

    for node in list:
        res += "<account id=\"" + node.account + "\">" + node.share + "</account>\n"

    res += "</symbol>"
    return res

"""
generate symbol list 
"""
def geneSymList(num):
    myList = []
    for j in range(0, num):
        myList.append(Node(geneRandAccNum(), str(randint(1000, 5000))))

    return myList

def getAccountId(line):
    j = 0
    while line[j] != '"':
        j += 1
    accountId = ""
    j += 1
    while line[j] != '"':
        accountId += str(line[j])
        j += 1
    return accountId

def processLines(lines, errorTest):
    # this is what I expect
    res = []
    index = 0
    balanceID   = "balance="
    accountID   = "<account id="
    symbolID    = "symbol sym"
    endSymID    = "/symbol"
    while index < len(lines):
        if balanceID in lines[index]:
            accountId = getAccountId(lines[index].strip())

            if errorTest:
                target = "<error id=\"" + str(accountId) + "\">Account already exists</error>"
            else:
                target = "<created id=\"" + accountId + "\"/>"
            res.append(target)

        elif symbolID in lines[index] and not errorTest:
            j = 0
            while lines[index][j] != '"':
                j += 1
            j += 1

            symbol = ""
            while lines[index][j] != '"':
                symbol += str(lines[index][j])
                j += 1

            index += 1

            while index < len(lines) and (endSymID not in lines[index]):
                line = lines[index].strip()
                if accountID in line:
                    account = getAccountId(line)
                    target = "<created sym=\"" + symbol + "\" id=\"" + account + "\"/>"
                    res.append(target)
                index += 1

        index += 1

    return res

def generateWorkLoad():
    res = ""
    myList = geneSymList(WORK_LOAD)

    ccList = []
    csList = []
    for i in range(0, WORK_LOAD):
        cc = geneCreateAccountClause(myList[i].account, myList[i].share)
        ccList.append(cc)

        sym = ""
        if i % 2 == 0:
            sym = "BTC"
        else:
            sym = "BullShit"

        sc = geneCreateSymClause(sym, myList)
        csList.append(sc)

    res += "<create>\n"

    for line in ccList:
        res += line
    for line in csList:
        res += line

    res += "</create>\n"

    return res

def writeToFile(msg, seq):
    length = len(msg)

    textFile = open("./txt/msg" + str(seq) + ".txt", 'w')
    textFile.write(str(length) + "\n")
    textFile.write(msg)
    textFile.close()

def prepareDoc():
    for i in range(0, WORK_LOAD):
        msg = generateWorkLoad()
        writeToFile(msg, i)

def compare(msgFile, outFile, errorTest):
    file1 = open(msgFile, 'r')
    file2 = open(outFile, 'r')
    lines1 = file1.readlines()
    lines2 = file2.readlines()

    # Strips the newline character
    expected = processLines(lines1, errorTest)
    index1 = 0
    index2 = 0

    while index1 < len(expected) and index2 < len(lines2):
        if expected[index1] in lines2[index2]:
            index1 += 1
            index2 += 1
        else:
            index2 += 1

    if index1 == len(expected):
        pass
    else:
        print("error found in " + msgFile + " AND " + outFile)
        sys.exit(1)



"""
Test in normal situation, if they can work correctly
"""
def testCorrectness():
    # step1: run generate cases
    prepareDoc()

    # step2: run engine and match
    cmd = './test.sh ' + str(WORK_LOAD)
    os.system(cmd)

    # step3: check if output is OK
    for i in range(0, WORK_LOAD):
        msg = './txt/msg' + str(i) + '.txt'
        out = './txt/out' + str(i) + '.txt'

        compare(msg, out, False)
    print("correctness passed")

"""
the idea is to first execute the command
but instead of check it immediately, we do that again
and thus the output should not be correct
"""
def testCreateDuplicateAccount():
    # step1: first we do it in normal way
    testCorrectness()

    # step2: we do that command again, check if we have exist
    cmd = './test.sh ' + str(WORK_LOAD)
    os.system(cmd)

    for i in range(0, WORK_LOAD):
        msg = './txt/msg' + str(i) + '.txt'
        out = './txt/out' + str(i) + '.txt'

        compare(msg, out, True)
    print("error test passed ")

def testBadRequest():
    HOST = "127.0.0.1"
    PORT = 12345

    badRequests = ["<create> </", "<create> </create>", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"]
    for i in range(0, len(badRequests)):
        print("send bad request " + badRequests[i])
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect((HOST, PORT))
            s.sendall(badRequests[i].encode())
            data = s.recv(1024)
            s.close()

            decodeData = data.decode()
            if "Bad Request" in decodeData:
                pass
            else:
                print("bad request with server no error flag")
                sys.exit(1)
    print("pass bad request test")


"""
Run all kinds of tests
"""
def main():
    print("====================test correctness=======================")
    testCorrectness()
    print("===========================================================\n\n")

    print("====================test error duplicate===================")
    testCreateDuplicateAccount()
    print("===========================================================\n\n")

    print("====================test bad request=======================")
    testBadRequest()
    print("===========================================================\n\n")

    print("--- all passed ---")

    choice = input("do you want to delete the test txt files? [y/n] default no ")

    if choice == "y":
        os.system('chmod 777 clear.sh && ./clear.sh')
        print("file all cleared")
    else:
        print("file saved")

if __name__ == '__main__':
    main()
