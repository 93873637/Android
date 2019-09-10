package com.liz.androidutils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@SuppressWarnings("unused, WeakerAccess")
public class LJson {

    public static String readJsonFile(String fileName) {
        String jsonStr;
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String compactJson(@NonNull String jsonStr) {
        return StrUtils.deleteCharsOutsideQuotes(jsonStr, " \n\t");
    }

    //
    //Format json string From:
    //{TYPE: 1, NAME: "whatsai", LIST: [{TYPE: 0, NAME: "TaskName0"},{TYPE: 0, NAME: "TaskName1"}]}
    //To:
    //{
    //  TYPE:1,
    //  NAME:"whatsai",
    //  LIST:[
    //    {
    //      TYPE:0,
    //      NAME:"TaskName0"
    //    },
    //    {
    //      TYPE:0,
    //      NAME:"TaskName1"
    //    }
    //  ]
    //}
    public static String formatJson(String jsonStr) {
        String srcStr = compactJson(jsonStr);
        if (null == srcStr || "".equals(srcStr)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        char last;
        char current = '\0';
        int indent = 0;
        for (int i = 0; i < srcStr.length(); i++) {
            last = current;
            current = srcStr.charAt(i);
            switch (current) {
                case '{':
                case '[':
                    sb.append(current);
                    sb.append('\n');
                    indent++;
                    addIndentSpace(sb, indent);
                    break;
                case '}':
                case ']':
                    sb.append('\n');
                    indent--;
                    addIndentSpace(sb, indent);
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\') {
                        sb.append('\n');
                        addIndentSpace(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }
        return sb.toString();
    }

    public static void addIndentSpace(StringBuilder sb, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append("  ");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {
        String jstr = "    {\n" +
                "      \"TYPE\":1,\n" +
                "      \"NAME\":\"what \\\"sai\\\" \",\n" +
                "      \"LIST\":[\n" +
                "        {\n" +
                "          \"TYPE\":0,\n" +
                "          \"NAME\":\"Task NAME \t 0\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"TYPE\":0,\n" +
                "          \"NAME\":\"Task NAME \t 1\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }";
        String compatStr = compactJson(jstr);
        System.out.println(compatStr);

//        String jstr = "";{TYPE: 1, NAME: "whatsai", LIST: [{TYPE: 0, NAME: "TaskName0"},{TYPE: 0, NAME: "TaskName1"}]}
//        String s = readJsonFile("D:/Temp/whatsai.dat");
//        System.out.println(s) ;
//
//        com.alibaba.fastjson.JSONObject jobj = com.alibaba.fastjson.JSON.parseObject(s);
//        System.out.println("TYPE: " + jobj.get("TYPE"));
//        System.out.println("NAME: " + jobj.get("NAME"));
//
//        try {
//            com.alibaba.fastjson.JSONArray links = jobj.getJSONArray("LIST");
//            for (int i = 0; i < links.size(); i++) {
//                com.alibaba.fastjson.JSONObject key1 = (com.alibaba.fastjson.JSONObject) links.get(i);
//                System.out.println("TYPE: " +  key1.get("TYPE"));
//                System.out.println("NAME: " +  key1.get("NAME"));
//            }
//        }
//        catch (Exception e) {
//            System.out.println(e.toString());
//        }
    }
}
