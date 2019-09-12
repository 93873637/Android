package com.liz.androidutils.test;

public class EnumTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static int ListMenuEnumID = 0;

    public enum ListMenu {
        OPEN("OPEN"),
        ADD("ADD"),
        MODIFY("MODIFY"),
        DEL("DELETE"),
        PROP("PROPERTIES");

        private int id;
        private String name;
        ListMenu(String name) {
            this.name = name;
            this.id = (ListMenuEnumID++);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        for (ListMenu c : ListMenu.values()) {
            System.out.println(c.id + " -- " + c.name);
        }

        test_switch(ListMenu.OPEN.id);
        test_switch(ListMenu.ADD.id);
        test_switch(ListMenu.MODIFY.id);
    }

    public static void test_switch(int a) {
        if (a == ListMenu.OPEN.id) {
            System.out.println("this is OPEN");
        } else if (a == ListMenu.ADD.id) {
            System.out.println("this is ADD");
        } else if (a == ListMenu.MODIFY.id) {
            System.out.println("this is MODIFY");
        }
    }
}
