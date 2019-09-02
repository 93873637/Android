package com.liz.whatsai.storage;

import android.text.TextUtils;
import android.util.Xml;

import com.liz.androidutils.LogUtils;
import com.liz.whatsai.logic.ComDef;
import com.liz.whatsai.logic.Node;
import com.liz.whatsai.logic.Reminder;
import com.liz.whatsai.logic.Task;
import com.liz.whatsai.logic.WhatsaiDir;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * WhatsaiStorage:
 * Created by liz on 2019/2/15.
 */

@SuppressWarnings("unused")
class StorageJSON {
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
