package com.liz.androidutils;

import androidx.annotation.NonNull;

@SuppressWarnings("unused, WeakerAccess")
public class StrUtils {

    public static String removeChars(@NonNull String srcStr, @NonNull String delChars) {
        String tarStr = srcStr;
        for (int i = 0; i < delChars.length(); i++) {
            String delStr = "" + delChars.charAt(i);
            tarStr = tarStr.replace(delStr, "");

        }
        return tarStr;
    }

    public static String deleteChar(@NonNull String srcStr, char delChar) {
        String tarStr = "";
        for (int i = 0; i < srcStr.length(); i++) {
            if(srcStr.charAt(i) != delChar){
                tarStr += srcStr.charAt(i);
            }
        }
        return tarStr;
    }

    public static String deleteChars(@NonNull String srcStr, @NonNull String delChars) {
        String tarStr = srcStr;
        for (int i = 0; i < delChars.length(); i++) {
            tarStr = deleteChar(tarStr, delChars.charAt(i));
        }
        return tarStr;
    }

    public static String deleteCharOutsideQuotes(@NonNull String srcStr, char delChar) {
        String tarStr = "";
        boolean inQuotes = false;
        for (int i = 0; i < srcStr.length(); i++) {
            char ch = srcStr.charAt(i);
            char pre_ch = (i==0)?' ':srcStr.charAt(i-1);
            if (ch == '"' && pre_ch != '\\') {
                inQuotes = !inQuotes;
            }
            if (inQuotes || srcStr.charAt(i) != delChar) {
                    tarStr += ch;
            }
        }
        return tarStr;
    }

    public static String deleteCharsOutsideQuotes(@NonNull String srcStr, @NonNull String delChars) {
        String tarStr = srcStr;
        for (int i = 0; i < delChars.length(); i++) {
            tarStr = deleteCharOutsideQuotes(tarStr, delChars.charAt(i));
        }
        return tarStr;
    }

//    public static final int INT_STR_LEN = 12;
//
//    public static String formatNum(int n) {
//        String ns = "" + n;
//        if (ns.length())
//            return ns;
//    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {
        {
            String aa = "  aa bb c";
            String bb = deleteCharsOutsideQuotes(aa, " \n\t");
            System.out.println("***\"" + aa + "\" -> \"" + bb + "\"");
        }
        {
            String aa = "  a\ta \nbb\t c";
            String bb = deleteCharsOutsideQuotes(aa, " \n\t");
            System.out.println("***\"" + aa + "\" -> \"" + bb + "\"");
        }
        {
            String aa = "  a\"\ta \nb\"b\t c";
            String bb = deleteCharsOutsideQuotes(aa, " \n\t");
            System.out.println("***\"" + aa + "\" -> \"" + bb + "\"");
        }

//
//        {
//            String aa = "{\"TYPE\": 1, \"NAME\": \"w'ha   t'sai\", \"LIST\": [{TYPE: 0, NAME: \"Task  Na\nme   0\"},{TYPE: 0, NAME: \"Tas \n\t  kName1\"}]}";
//            String bb = deleteCharsOutsideQuotes(aa, " \n\t");
//            System.out.println("\"" + aa + "\" -> \"" + bb + "\"");
//        }

//        String aa = "  a a   a \n d d \t";
//        String bb = removeChars(aa, " \n\t");
//        System.out.println("\"" + aa + "\" -> \"" + bb + "\"") ;
//        String cc = removeChars(aa, " \n\t");
//        System.out.println("\"" + aa + "\" -> \"" + cc + "\"") ;
    }
}
