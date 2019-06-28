package com.liz.screenhelper.utils;

import java.util.LinkedList;

public class StringQueue extends LinkedList<String>{

    public static void main(String[] args) {
        System.out.println("StringQueue: Enter...");
        StringQueue queue = new StringQueue();
        queue.add("a");
        queue.add("b");
        queue.add("c");
        queue.add("d");
        queue.add("e");
        System.out.println("queue=" + queue + ", size=" + queue.size());

        System.out.println(queue.poll());
        System.out.println("queue=" + queue + ", size=" + queue.size());
        System.out.println(queue.poll());
        System.out.println("queue=" + queue + ", size=" + queue.size());
        System.out.println(queue.poll());
        System.out.println("queue=" + queue + ", size=" + queue.size());
        System.out.println(queue.poll());
        System.out.println("queue=" + queue + ", size=" + queue.size());
        System.out.println(queue.poll());
        System.out.println("queue=" + queue + ", size=" + queue.size());

        //queue.clear();
        //System.out.println(queue.size());
    }
}
