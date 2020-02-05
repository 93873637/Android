#!/usr/bin/env python
# -*- coding: utf-8 -*

from __future__ import print_function

import os
import sys


ARGC_MIN = 2


#
# Write your main func here
# 
def main_func(argv):
    
    num_count = int(argv[1])
    f = open("M03_000001.txt", 'w')

    for i in range(num_count):
        num = 17238807501 + i
        f.write(str(num) + "\n")
    
    f.close()
#def main_func


#################################################################
# STANDARD MAIN BODY
#################################################################


def usage(app_name):
    print("")
    print("Usage:")
    print(app_name + " number_count")
    print("")
# def usage


def get_cmd_line():
    ret = "***"
    arg_num = len(sys.argv)
    for i in range(0, arg_num):
        ret += sys.argv[i]
        if (i != arg_num - 1):
            ret += " "
    return ret
# def get_cmd_line


if __name__ == '__main__':
    print("\n" + get_cmd_line() + ", Enter...\n")
    
    if (len(sys.argv) < ARGC_MIN):
        usage(sys.argv[0])
        exit(-1)

    main_func(sys.argv)

    print("\n" + get_cmd_line() + ", Exit.")
    #os.system('pause')
