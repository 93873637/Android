package com.liz.whatsai.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TaskGroup.java
 * Created by admin on 2018/9/17.
 */

public class TaskGroup extends WhatsaiDir {
    private ArrayList<Node> list = null;
    private int task_number = 0;

    public TaskGroup() {
        super();
        list = new ArrayList<>();
        task_number = 0;
    }

    public TaskGroup(String name) {
        super(name);
        list = new ArrayList<>();
        task_number = 0;
    }

    @Override
    public boolean isDone() {
        if (list.size() == 0) {
            return false;
        }

        //check all its child tasks done
        for (Node node: list) {
            //not done if one child not done
            if (!node.isDone())
                return false;
        }

        //all of its child tasks done
        return true;
    }

    @Override
    public int getType() {
        return ComDef.NODE_TYPE_TASKGROUP;
    }

    @Override
    public List<Node> getList() {
        Collections.sort(list);
        return list;
    }

    @Override
    public void incTaskNumber(int num) {
        task_number += num;
    }

    @Override
    public void decTaskNumber(int num) {
        task_number -= num;
    }

    @Override
    public int getTaskNumber() {
        return task_number;
    }

    public void add(Node node) {
        list.add(node);
        node.setParent(this);

        int tn = node.getTaskNumber();
        this.incTaskNumber(tn);
        Node iter = this;
        while ((iter = iter.getParent()) != null) {
                iter.incTaskNumber(tn);
        }
    }

    public void remove(int pos) {
        Node node = list.get(pos);
        int tn = node.getTaskNumber();

        list.remove(pos);

        this.decTaskNumber(tn);
        Node iter = this;
        while ((iter = iter.getParent()) != null) {
            iter.decTaskNumber(tn);
        }
    }

    public Node get(int pos) {
        return list.get(pos);
    }

    private int statDoneNumber() {
        int count = 0;
        for (Node node: list) {
            //not done if one child not done
            if (node.isDir()) {
                count += ((TaskGroup)node).statDoneNumber();
            }
            else {
                if (node.isDone())
                    count++;
            }
        }
        return count;
    }

    public String getNameEx() {
        return this.getName() + "(" + statDoneNumber() + "/" + task_number + ")";
    }
}
