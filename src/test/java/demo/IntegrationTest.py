#!/usr/bin/env python3
import socket
import sys
import os
from random import randint
import time

ACCOUNT_LEN = 20
WORK_LOAD   = 20
REQUEST_NUM = 30

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
This is used to generate the transaction clause
"""
def geneTranClause(sym, accountId):

    # create account 123 with 100 spy
    xml1 = "<?xml version = \"1.0\"?> <create> <account id=\"" + str(accountId) + "\" balance=\"15364\"/>\n"
    xml1 += "<account id=\"234\" balance=\"56478\"/>\n"
    xml1 += "<symbol sym=\"" + str(sym) + "\"> <account id=\"" + str(accountId) + "\">100</account> </symbol>\n"
    xml1 += "</create>\n"

    writeToFile(xml1, 101)

    # Account 123 tries to sell 50 SPY (should success) and then 51 SPY
    xml2 = "<?xml version = \"1.0\"?> <transactions id=\"" + str(accountId) + "\">\n"
    xml2 += "<order sym=\"" + str(sym) + "\" amount=\"-50\" limit=\"200\"/>\n"
    xml2 += "<order sym=\"" + str(sym) + "\" amount=\"-51\" limit=\"14\"/>\n"
    xml2 += "</transactions>\n"

    writeToFile(xml2, 102)

    xml3 = "<?xml version = \"1.0\"?> <transactions id=\"234\">\n"
    xml3 += "<order sym=\"" + str(sym) + "\" amount=\"20\" limit=\"210\"/> \n"
    xml3 += "</transactions>\n"

    writeToFile(xml3, 103)

    xml4 = "<?xml version = \"1.0\"?> <transactions id=\"" + str(accountId) + "\">\n"
    xml4 += "<order sym=\"" + str(sym) + "\" amount=\"-10\" limit=\"14\"/> \n"
    xml4 += "<query id=\"2\"/> \n"
    xml4 += "</transactions>\n"

    writeToFile(xml4, 104)

    xml5 = "<?xml version = \"1.0\"?> <transactions id=\"" + str(accountId) + "\">\n"
    xml5 += "<cancel id=\"2\"/>\n"
    xml5 += "</transactions>\n"

    writeToFile(xml5, 105)

    xml6 = "<?xml version = \"1.0\"?> <transactions id=\"" + str(accountId) + "\">\n"
    xml6 += "<cancel id=\"2\"/> \n"
    xml6 += "</transactions>\n"

    writeToFile(xml6, 106)

    xml7 = "<?xml version = \"1.0\"?> <transactions id=\"" + str(accountId) + "\">\n"
    xml7 += "<query id=\"2\"/>\n"
    xml7 += "</transactions>\n"

    writeToFile(xml7, 107)
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
    myList = geneSymList(REQUEST_NUM)

    ccList = []
    csList = []
    for i in range(0, REQUEST_NUM):
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
    cmd = 'rm -rf /txt/*'
    mkdir = 'mkdir -p txt'

    os.system(cmd)
    print("clear all files in txt")
    os.system(mkdir)
    print("make a clear /txt directory")

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


def testTran():
    geneTranClause("STM", 123)

    cmd = "./test.sh " + str(108) + " " + str(101)
    os.system(cmd)

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

    print("====================test transaction========================\n\n")
    testTran()
    print("===========================================================\n\n")

    print("====================test time========================\n\n")
    timeTest()
    print("===========================================================\n\n")

    print("--- all passed ---")

    choice = input("do you want to delete the test txt files? [y/n] default no ")

    if choice == "y":
        os.system('chmod 777 clear.sh && ./clear.sh')
        print("file all cleared")
    else:
        print("file saved")


def timeTest():
    print("request per message: " + str(REQUEST_NUM))
    global WORK_LOAD
    for i in range(1, 10):
        WORK_LOAD = i * 5
        print("now workload is " + str(WORK_LOAD))
        startTime = time.time()
        testCorrectness()
        print("--- %s seconds ---" % (time.time() - startTime))

        print("sleep the thread to cool down")
        time.sleep(3)
        print("\n")

if __name__ == '__main__':
    main()
    #testTran()

