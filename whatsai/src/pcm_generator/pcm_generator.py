#!/usr/bin/env python
# -*- coding: utf-8 -*

import os
import sys

VAL_LEN = 1024

pcmFilePath = 'd:/temp/test.pcm'
pcmFile = open(pcmFilePath, 'wb+')


for d in range(0, 256):
    for i in range(0, VAL_LEN):
        buf = d.to_bytes(1, 'big')
        pcmFile.write(buf)

pcmFile.close()
#os.system("pause")
