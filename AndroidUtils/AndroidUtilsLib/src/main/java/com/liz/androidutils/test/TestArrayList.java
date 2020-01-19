package com.liz.androidutils.test;

import java.util.ArrayList;
import java.util.Iterator;

public class TestArrayList {

    private static ArrayList<FileUploadTask> list = new ArrayList<>();

    private static class FileUploadTask {
        public String fileName;

        FileUploadTask(String fileName) {
            this.fileName = fileName;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // main func

    public static void main(String[] args) {
        test_remove();
    }

    public static void print_list(String name) {
        System.out.println("----------------------------------------------------");
        System.out.println(name + ": list size = " + list.size());
        for (int i = 0; i< list.size(); i++) {
            System.out.println("#" + i + ": " + list.get(i).fileName);
        }
        System.out.println("----------------------------------------------------");
    }

    public static void test_remove() {
        list.add(new FileUploadTask("111"));
        list.add(new FileUploadTask("222"));
        list.add(new FileUploadTask("333"));
        print_list("BEGIN");

        /* ConcurrentModificationException
        for (FileUploadTask task : list) {
            list.remove(task);
            print_list("remove task: " + task.fileName);
        }
        //*/

        /*
        if (null != list && list.size() > 0) {
            Iterator it = list.iterator();
            while(it.hasNext()){
                FileUploadTask obj = (FileUploadTask)it.next();
                //System.out.println(obj.fileName);
                it.remove(); //移除该对象
                print_list("remove task " + obj.fileName);
            }
        }
        //*/

        System.out.println(list.get(0).fileName);
        list.remove(0);


        print_list("END");
    }
}
