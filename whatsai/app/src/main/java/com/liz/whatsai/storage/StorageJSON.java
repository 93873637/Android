package com.liz.whatsai.storage;

import com.alibaba.fastjson.JSON;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.liz.whatsai.logic.WhatsaiDir;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * WhatsaiStorage:
 * Created by liz on 2019/2/15.
 */

@SuppressWarnings("unused")
class StorageJSON {

    public static void saveToJSON(OutputStream out, WhatsaiDir rootNode) throws Exception {
        Gson gson = new Gson();
        String jsonString = gson.toJson(rootNode);
        System.out.println(jsonString);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static class User {
        public User() {

        }
        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
        public String name;
        public int age;

        @SerializedName("email_address")
        public String emailAddress;

        public List<User> children = new ArrayList<>();

        //因业务需要增加，但并不需要序列化
        public User parent;
    }

    public static void main(String[] args) {
        test_Gson();
//        WhatsaiDir node = new WhatsaiDir("aaa");
//        WhatsaiDir subNode = new WhatsaiDir("bbb");
//        node.add(subNode);

        /* this can only run on android system, or you will get exception Stub!
        try {
            JSONObject object = new JSONObject();
            //string
            object.put("string", "this is my string");
            //int
            object.put("int", 2);
            //boolean
            object.put("boolean", true);
            //array
            List<Integer> integers = Arrays.asList(1, 2, 3);
            object.put("list", integers);
            //null
            object.put("null", null);
            System.out.println(object);
        }
        catch (Exception e) {
            System.out.println("JSONObject Exception: e=" + e.toString());
        }
        //*/

        /*
        {
            Gson gson = new Gson();
            String jsonString = gson.toJson(node);
            System.out.println("gson=" + jsonString);
        }
        //*/

        /*
        {
            String jsonString = JSON.toJSONString(node.getRemindString());
            System.out.println("fastJson=" + jsonString);
        }
        //*/
    }

    public static class UserTypeAdapter extends TypeAdapter<User> {

        @Override
        public void write(JsonWriter out, User value) throws IOException {
            out.beginObject();
            write_obj(out, value);
            out.endObject();
        }

        public void write_obj(JsonWriter out, User value) throws IOException {
            out.name("name").value(value.name);
            out.name("age").value(value.age);
            out.name("email").value(value.emailAddress);
            for (int i=0; i<value.children.size(); i++) {
                write_obj(out, value.children.get(i));
            }
        }

        @Override
        public User read(JsonReader in) throws IOException {
            User user = new User();
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "name":
                        user.name = in.nextString();
                        break;
                    case "age":
                        user.age = in.nextInt();
                        break;
                    case "email":
                    case "email_address":
                    case "emailAddress":
                        user.emailAddress = in.nextString();
                        break;
                }
            }
            in.endObject();
            return user;
        }
    }

    public static void test_Gson() {
//        Gson gson = new Gson();
//        int i = gson.fromJson("100", int.class);              //100
//        double d = gson.fromJson("\"99.99\"", double.class);  //99.99
//        boolean b = gson.fromJson("true", boolean.class);     // true
//        String str = gson.fromJson("String", String.class);   // String

        {
            Gson gson = new Gson();
            User user = new User("怪盗kidou", 24);
            user.emailAddress = "aaaaaa";
            String jsonString = gson.toJson(user); // {"name":"怪盗kidou","age":24}
            System.out.println("jsonObject=" + jsonString);
        }
        {
            Gson gson = new Gson();
            String jsonString = "{\"name\":\"怪盗kidou\",\"age\":24,\"emailAddress\":\"ikidou@example.com\"}";//{"name":"怪盗kidou","age":24,"emailAddress":"ikidou@example.com"};  //"{\"name\":\"怪盗kidou\",\"age\":24}";
            User user = gson.fromJson(jsonString, User.class);
            System.out.println("jsonObject.emailAddress=" + user.emailAddress);
        }
        {
            try {
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(System.out));
                writer.beginObject() // throws IOException
                        .name("name").value("怪盗kidou")
                        .name("age").value(24)
                        .name("email").nullValue() //演示null
                        .endObject(); // throws IOException
                writer.flush(); // throws IOException
                System.out.println("");
            }
            catch(Exception e){

            }
        }
        {
            User user = new User("怪盗kidou", 44);
            user.emailAddress = "ikidou@example.com";
            User user2 = new User("user2", 20);
            user.children.add(user2);
            user2.parent = user;

            Gson gson = new GsonBuilder()
                    //为User注册TypeAdapter
                    .registerTypeAdapter(User.class, new UserTypeAdapter())
                    .create();
            System.out.println("gson TypeAdapter=" + gson.toJson(user));
        }
//        User user = new User("怪盗kidou", 24);
//        user.emailAddress = "ikidou@example.com";
//        Gson gson = new GsonBuilder()
//                //为User注册TypeAdapter
//                .registerTypeAdapter(User.class, new UserTypeAdapter())
//                .create();
//        System.out.println(gson.toJson(user));
    }


//
//    public static RespQueryMdn parseResponseQueryMdn(String resStr) {
//        RespQueryMdn resp = new RespQueryMdn();
//
//        try {
//            JSONObject obj = new JSONObject(resStr);
//            int resCode = obj.optInt("resCode");
//            LogUtils.d("parseResponseQueryMdn: resCode = " + resCode);
//            switch(resCode) {
//                case REQ_QUERY_MDN_SUCCESS:
//                    String resMsg = obj.optString("resMsg");
//                    String dataStr = obj.optString("data");
//                    LogUtils.d("resMsg: " + resMsg);
//                    LogUtils.d("data: " + dataStr);
//                    if (TextUtils.isEmpty(dataStr)) {
//                        LogUtils.e("ERROR: parseResponseQueryMdn: data empty");
//                        resp.errMsg = "RESPONSE DATA EMPTY";
//                    } else {
//                        JSONObject dataObj = new JSONObject(dataStr);
//                        String status = dataObj.optString("status");
//                        LogUtils.i("parseResponseQueryMdn: status = " + status);
//                        if (TextUtils.equals(status, MDN_STATUS_ACTIVE)) {
//                            String nodeUrl = dataObj.optString("url");
//                            if (TextUtils.isEmpty(nodeUrl)) {
//                                resp.errMsg = "URL EMPTY";
//                            }
//                            else {
//                                resp.url = nodeUrl;
//                                resp.errCode = RESP_ERR_SUCCESS;
//                            }
//                        }
//                        else {
//                            resp.errMsg = "对不起，该手机号码业务已取消";
//                        }
//                    }
//                    break;
//                case REQ_QUERY_MDN_ERROR:
//                    resp.errMsg = "QUERY ERROR";
//                    break;
//                case REQ_QUERY_MDN_NOT_FOUND:
//                    resp.errMsg = "对不起，该手机号码尚未开通该业务";
//                    break;
//                case REQ_SERVER_ERROR:
//                    resp.errMsg = "SERVER ERROR";
//                    break;
//                default:
//                    LogUtils.e("ERROR: parseResponseQueryMdn: Unknown resCode " + resCode);
//                    resp.errMsg = "";
//                    break;
//            }
//        } catch (Exception e) {
//            LogUtils.e("parseResponseQueryMdn Exception: " + e.toString());
//            e.printStackTrace();
//        }
//
//        return resp;
//    }
//
//
//
//
//
//    private static String ENTER = System.getProperty("line.separator");
//    private static final int INDENT_LEFT_START = 0;
//    private static final int INDENT_INCREMENT = 4;
//
//    private static String xmlPullParserTag[] = {
//            "START_DOCUMENT",
//            "END_DOCUMENT",
//            "START_TAG",
//            "END_TAG",
//            "TEXT",
//    };
//
//    static void loadFromFile(InputStream xmlStream, WhatsaiDir rootNode) throws Exception {
//        XmlPullParser pullParser = Xml.newPullParser();
//        pullParser.setInput(xmlStream, "UTF-8");
//
//        Node curDir = rootNode;
//        Node newNode;
//        String attrValue;
//
//        int event = pullParser.getEventType();
//        String tagName;
//        while (event != XmlPullParser.END_DOCUMENT) {
//            tagName = pullParser.getName();
//            LogUtils.v("loadFromXML: event=" + xmlPullParserTag[event] + ", tagName=" + tagName);
//
//            switch (event) {
//                case XmlPullParser.START_DOCUMENT:
//                    //we already have rootNode, so here do nothing
//                    //taskGroup = new WhatsaiDir();
//                    break;
//                case XmlPullParser.START_TAG:
//                    if (ComDef.XML_TAG_DIR.equals(tagName)) {
//                        //ENTER into a new taskgroup
//                        newNode = new WhatsaiDir();
//                        attrValue = pullParser.getAttributeValue(null, ComDef.XML_ATTR_NAME);
//                        newNode.setName(attrValue);
//                        LogUtils.v("loadFromXML: enter taskgroup, name=" + attrValue);
//                        curDir.add(newNode);
//                        curDir = newNode;
//                    } else if (ComDef.XML_TAG_FILE.equals(tagName)) {
//                        //ENTER into a new task
//                        newNode = new Task();
//                        attrValue = pullParser.getAttributeValue(null, ComDef.XML_ATTR_NAME);
//                        newNode.setName(attrValue);
//                        LogUtils.v("loadFromXML: enter task, name=" + attrValue);
//                        attrValue = pullParser.getAttributeValue(null, ComDef.XML_ATTR_DETAIL);
//                        newNode.detail = attrValue;
//                        //LogUtils.v("loadFromXML: enter task, detail=" + attrValue);
//                        attrValue = pullParser.getAttributeValue(null, ComDef.XML_ATTR_DONE);
//                        newNode.setDone(TextUtils.equals(attrValue, ComDef.XML_BOOL_TRUE));
//                        //LogUtils.v("loadFromXML: enter task, done=" + attrValue);
//                        attrValue = pullParser.getAttributeValue(null, ComDef.XML_ATTR_REMIND);
//                        newNode.setRemindString(attrValue);
//                        Reminder.checkRemind(newNode);
//                        //LogUtils.v("loadFromXML: enter task, remind=" + attrValue);
//                        curDir.add(newNode);
//                    } else {
//                        LogUtils.e("loadFromXML: Unknown START_TAG: " + tagName);
//                    }
//                    break;
//                case XmlPullParser.END_TAG:
//                    if (ComDef.XML_TAG_DIR.equals(tagName)) {
//                        //exit current group, back to upper group
//                        LogUtils.v("loadFromXML: exit taskgroup, name=" + curDir.getName());
//                        curDir = curDir.getParent();
//                    } else if (ComDef.XML_TAG_FILE.equals(tagName)) {
//                        //exit current task, change current node to group
//                        LogUtils.v("loadFromXML: exit task, name=" + curDir.getName());
//                    } else {
//                        LogUtils.e("loadFromXML: Unknown END_TAG: " + tagName);
//                    }
//                    break;
//                case XmlPullParser.TEXT:
//                    LogUtils.v("loadFromXML: getText: \"" + pullParser.getText() + "\"");
//                    break;
//                default:
//                    LogUtils.w("loadFromXML: Unhandled event: " + event);
//                    break;
//            }
//
//            event = pullParser.next();
//        }
//    }
//
//    static void saveToXML(OutputStream out, WhatsaiDir rootNode) throws Exception {
//        XmlSerializer serializer = Xml.newSerializer();
//        serializer.setOutput(out, "UTF-8");
//        serializer.startDocument("UTF-8", true); endLine(serializer, ENTER);
//
//        for (Node node : rootNode.getList()) {
//            writeNode(serializer, node, INDENT_LEFT_START);
//        }
//
//        serializer.endDocument();
//    }
//
//    private static void writeNode(XmlSerializer s, Node node, int indent) throws Exception {
//        if (node.isDir()) {
//            newLine(s, indent);
//            s.startTag(null, ComDef.XML_TAG_DIR);
//            s.attribute(null, ComDef.XML_ATTR_NAME, node.getName()); endLine(s, ENTER);
//            for (Node subNode : node.getList()) {
//                writeNode(s, subNode, indent+ INDENT_INCREMENT);
//            }
//            newLine(s, indent); s.endTag(null, ComDef.XML_TAG_DIR); endLine(s, ENTER);
//        }
//        else {
//            newLine(s, indent);
//            s.startTag(null, ComDef.XML_TAG_FILE);
//            s.attribute(null, ComDef.XML_ATTR_NAME, node.getName()==null?"":node.getName());
//            s.attribute(null, ComDef.XML_ATTR_DETAIL, node.detail==null?"":node.detail);
//            if (node.isDone()) {
//                s.attribute(null, ComDef.XML_ATTR_DONE, ComDef.XML_BOOL_TRUE);
//            }
//            s.attribute(null, ComDef.XML_ATTR_REMIND, node.getRemindString()==null?"":node.getRemindString());
//            s.endTag(null, ComDef.XML_TAG_FILE);
//            endLine(s, ENTER);
//        }
//    }
//
//    private static void newLine(XmlSerializer s, int indent){
//        try{
//            StringBuilder spaceIndent = new StringBuilder();
//            for (int i=0; i<indent; i++) spaceIndent.append(" ");
//            s.text(spaceIndent.toString());
//        }catch(IOException e){
//            System.out.println(e.getMessage());
//        }
//    }
//
//    private static void endLine(XmlSerializer s, String ENTER){
//        try{
//            s.text(ENTER);
//        }catch(IOException e){
//            System.out.println(e.getMessage());
//        }
//    }
}
