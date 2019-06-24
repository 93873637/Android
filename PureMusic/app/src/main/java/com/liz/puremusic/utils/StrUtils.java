package com.liz.puremusic.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StrUtils:
 * Created by liz on 2019/1/29.
 */

public class StrUtils {

    public static void main(String[] args) {
        String s1 = "abjdfhelloshdf";
        String s2 = "fhellods";
        sop(getMaxSubString(s1, s2));
    }

    private static void sop(String s) {
        System.out.println(s);
    }

    public static String getMaxSubString(String s1, String s2) {
        //最大相同子串，s1,s2=min
        String max = (s1.length() > s2.length()) ? s1 : s2;
        String min = (max == s1) ? s2 : s1;
        for (int x = 0; x < min.length(); x++) {
            for (int y = 0, z = min.length() - x; z != min.length() + 1; y++, z++) {
                String temp = min.substring(y, z);
                if (max.contains(temp))
                    return temp;
            }
        }

        return "";
    }

    /**
     * 传入2个字符串进行相比高亮显示
     * 例如
     * 原数据一:王五张三
     * 原数据二:张三李四
     * <span style='color:blue'>王五</span>张三
     * 张三<span style='color:blue'>李四</span>
     */
    public static String[] getHighLightDifferent(String a, String b) {
        String[] temp = getDiff(a, b);
        String[] result = {getHighLight(a, temp[0]), getHighLight(b, temp[1])};
        return result;
    }

    private static String getHighLight(String source, String temp) {
        StringBuffer sb = new StringBuffer();
        char[] sourceChars = source.toCharArray();
        char[] tempChars = temp.toCharArray();
        boolean flag = false;
        for (int i = 0; i < sourceChars.length; i++) {
            if (tempChars[i] != ' ') {
                if (i == 0) sb.append("<span style='color:blue'>").append(sourceChars[i]);
                else if (flag) sb.append(sourceChars[i]);
                else sb.append("<span style='color:blue'>").append(sourceChars[i]);
                flag = true;
                if (i == sourceChars.length - 1) sb.append("</span>");
            } else if (flag == true) {
                sb.append("</span>").append(sourceChars[i]);
                flag = false;
            } else sb.append(sourceChars[i]);
        }
        return sb.toString();
    }

    public static String[] getDiff(String a, String b) {
        String[] result = null;
        //选取长度较小的字符串用来穷举子串
        if (a.length() < b.length()) {
            result = getDiff(a, b, 0, a.length());
        } else {
            result = getDiff(b, a, 0, b.length());
            result = new String[]{result[1], result[0]};
        }
        return result;
    }

    //将a的指定部分与b进行比较生成比对结果
    private static String[] getDiff(String a, String b, int start, int end) {
        String[] result = new String[]{a, b};
        int len = result[0].length();
        while (len > 0) {
            for (int i = start; i < end - len + 1; i++) {
                String sub = result[0].substring(i, i + len);
                int idx = -1;
                if ((idx = result[1].indexOf(sub)) != -1) {
                    result[0] = setEmpty(result[0], i, i + len);
                    result[1] = setEmpty(result[1], idx, idx + len);
                    if (i > 0) {
                        //递归获取空白区域左边差异
                        result = getDiff(result[0], result[1], 0, i);
                    }
                    if (i + len < end) {
                        //递归获取空白区域右边差异
                        result = getDiff(result[0], result[1], i + len, end);
                    }
                    len = 0;//退出while循环
                    break;
                }
            }
            len = len / 2;
        }
        return result;
    }

    //将字符串s指定的区域设置成空格
    public static String setEmpty(String s, int start, int end) {
        char[] array = s.toCharArray();
        for (int i = start; i < end; i++) {
            array[i] = ' ';
        }
        return new String(array);
    }

    /*方法二：推荐，速度最快
  * 判断是否为整数
  * @param str 传入的字符串
  * @return 是整数返回true,否则返回false
    */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static int compareByInteger(String s1, String s2) {
        boolean isInt1 = isInteger(s1);
        boolean isInt2 = isInteger(s2);
        if (isInt1 && !isInt2) {
            return 1;
        }
        else if (!isInt1 && isInt2) {
            return -1;
        }
        else if (isInt1 && isInt2) {
            return Integer.parseInt(s1) - Integer.parseInt(s2);
        }
        else {
            return 0;
        }
    }

    public static int compareByBracketedInteger(String s1, String s2) {
        Pattern pattern = Pattern.compile(".*?\\((.*?)\\).*?");
        Matcher matcher1 = pattern.matcher(s1);
        Matcher matcher2 = pattern.matcher(s2);
        if (matcher1.matches() && !matcher2.matches()) {
            return 1;
        }
        else if (!matcher1.matches() && matcher2.matches()) {
            return -1;
        }
        else if (matcher1.matches() && matcher2.matches()) {
            String subStr1 = matcher1.group(1);
            String subStr2 = matcher2.group(1);
            return compareByInteger(subStr1, subStr2);
        }
        else {
            return 0;
        }
    }

    public static int compareByDiff(String s1, String s2) {
        return 0;
//                String[] diff = StrUtils.getDiff(s1, s2);
//                String diff1 = diff[0].trim();
//                String diff2 = diff[1].trim();
//                if (TextUtils.isEmpty(diff1) && !TextUtils.isEmpty(diff2)) {
//                    ret = 1;  //diff2 > diff1
//                }
//                else if (!TextUtils.isEmpty(diff1) && TextUtils.isEmpty(diff2)) {
//                    ret = -1;  //diff2 < diff1
//                }
//                else if (!TextUtils.isEmpty(diff1) && !TextUtils.isEmpty(diff2)) {
//                    if (StrUtils.isInteger(diff1) && StrUtils.isInteger(diff2)) {
//                        ret = Integer.parseInt(diff1) - Integer.parseInt(diff2);
//                    }
//                }
    }

}
