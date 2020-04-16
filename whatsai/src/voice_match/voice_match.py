# -*-coding:utf8-*-
import os
import re
import subprocess

# ��ȡtxt�ļ����ݣ�ÿ�зֿ����뵽list��
def readTxt(path, ignore):
    contentList = list()
    for line in open(path, "r"):
        line = re.sub(r'\n', '', line)
        if line.find(ignore) == -1:
            contentList.append(line)
    return contentList

def compareAudio(fileNameList, compareFile):
    # ����AudioCompare�ⷽ���Ƚ������ļ�
    fileName = compareFile.split('/')[-1]
    filePath = compareFile[:-len(fileName)]
    if not os.path.exists(filePath):
        os.mkdir(filePath)
    if os.path.exists(compareFile):  # �Ѵ���
        fout = open(compareFile, 'a')
    else:
        fout = open(compareFile, 'w')
        fout.write('# ��¼��Ƶ�ȽϽ�����ļ�\n')
    contentList = readTxt(compareFile, '#')
    for index, fileName in enumerate(fileNameList):
        for compareFileName in fileNameList[index + 1:]:
            # �Ӽ�¼���ļ���ȡֵ������Ѿ��ȽϹ��Ͳ��ٱȽ�
            needCompareFlag = True
            for content in contentList:
                if fileName in content.split('|') and compareFileName in content.split('|'):
                    needCompareFlag = False
                    print '�Ѵ��ڣ�' + fileName + '|' + compareFileName
            if needCompareFlag:
                command = ' AudioCompare-master/main.py -f ' + fileName + ' -f ' + compareFileName
                p = subprocess.Popen('python' + command, stdout=subprocess.PIPE, shell=True)
                stdoutput = p.stdout.readlines()
                fout.write(stdoutput[0])


# ��ȡ�ļ���С
def getFileSize(fileName):
    try:
        return os.path.getsize(fileName)
    except Exception as err:
        print(err)

# �õ��ļ���С���Ƶļ��ϣ�����[[('xx.wav',3715364L)],[('xx.wav',3715364L),('xx.wav',3715364L),('xx.wav',3715364L)]]
def sortedNearFile(fileNamesList, level):
    # �õ�ÿ���ļ��Ĵ�С
    fileSizeDict = dict()
    for fileName in fileNamesList:
        fileSizeDict.setdefault(fileName, getFileSize(fileName))
    # �Ȱ����ļ��Ĵ�С����
    sortedFileSizeList = sorted(fileSizeDict.iteritems(), key=lambda item: item[1])
    # ����Ƚ����ڵ��ļ���С��С����ֵ����ȡ����
    nearFileList = list()
    for index in range(0, len(sortedFileSizeList)):
        if index + 1 < len(sortedFileSizeList) and index > 0:
            # ����ߵı�
            preVal = abs(sortedFileSizeList[index][1] - sortedFileSizeList[index - 1][1])
            # ���ұ߱�
            nextVal = abs(sortedFileSizeList[index][1] - sortedFileSizeList[index + 1][1])
            # �ó�����߻��ұ���Ƶ��СС�ڷ�ֵ����Ƶ
            if preVal <= level or nextVal <= level:
                nearFileList.append(sortedFileSizeList[index])
    # ���ظ��ķ���һ���б��У�����[[('xx.wav',3715364L)],[('xx.wav',3715364L),('xx.wav',3715364L),('xx.wav',3715364L)]]
    sortedNearFileList = list()
    i = 0
    for index in range(0, len(nearFileList)):
        # ���nearFileList[index] - sortedNearFileList[i][0] <= level ����ӣ�������ӵ�sortedNearFileList[i+1]��
        if len(sortedNearFileList) == 0:
            sortedNearFileList.append([nearFileList[index]])
        else:
            # �ұ߼���ߣ�С�ڷ�ֵ�ı���
            val = abs(sortedNearFileList[i][0][1] - nearFileList[index][1])
            if val <= level:
                sortedNearFileList[i].append(nearFileList[index])
            else:
                i += 1
                sortedNearFileList.append([nearFileList[index]])
    return sortedNearFileList

# �ҳ�ָ���ļ����µ����к�׺��Ϊsuffix���ļ����ƣ������б�
def getFileNames(dirPath, suffix):
    fileNamesList = list()
    for fileName in os.listdir(dirPath):
        if os.path.splitext(fileName)[1] == suffix:
            fileNamesList.append(dirPath + '/' + fileName)
    return fileNamesList

if __name__ == '__main__':
    fileNameList = getFileNames(os.path.curdir, '.wav')
    sortedFileList = sortedNearFile(fileNameList, 100 * 1024)
    for fileTupleList in sortedFileList:
        if len(fileTupleList) > 1:
            compareList = list()
            for fileTuple in fileTupleList:
                compareList.append(fileTuple[0])
            compareAudio(compareList[:], './compare.txt')
