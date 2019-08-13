package com.liz.noannoy.logic;

import android.text.TextUtils;

import com.liz.androidutils.LogUtils;
import org.json.JSONObject;

@SuppressWarnings("unused, WeakerAccess")
public class DataAPI {

    public static final int RESP_ERR_UNKNOWN = -1;
    public static final int RESP_ERR_SUCCESS = 0;

    public static final int REQ_QUERY_MDN_SUCCESS = 1000;  //请求成功
    public static final int REQ_QUERY_MDN_ERROR = 1001;  //请求异常
    public static final int REQ_QUERY_MDN_NOT_FOUND = 1004; //无记录
    public static final int REQ_SERVER_ERROR = 5000;  //服务异常

    public static final String MDN_STATUS_ACTIVE = "1";  //开通
    public static final String MDN_STATUS_CANCEL = "2";  //取消


    public static class ResponseData {
        public int errCode;
        public String errMsg;

        public ResponseData() {
            errCode = RESP_ERR_UNKNOWN;
            errMsg = "";
        }
    }

    public static class RespQueryMdn extends ResponseData {
        public String url;
    }

    public static String getUrlQueryMdn(String telNum) {
        return "http://" + ComDef.CENTER_SERVER_IP + ":" + ComDef.CENTER_SERVER_PORT_HTTP
                + "/fsrserver/data/query?mdn=" + telNum;
    }

    //String respBody = "{\"resCode\":\"1000\", \"resMsg\":\"请求成功,查得\", \"data\":{\"mdn\":\"123123\",\"url\":\"http\",\"ctime\":\"2019-07-30 16:38:36\",\"utime\":\"2019-07-30 16:52:42\",\"mark\":\"\"}}";
    //String respBody = "{\"resCode\":\"1000\", \"resMsg\":\"请求成功,查得\", \"data\":{\"mdn\":\"123123\",\"url\":\"http://gzlt.dwsoft.com.cn:18080/ivr/\",\"ctime\":\"2019-07-30 16:38:36\",\"utime\":\"2019-07-30 16:52:42\",\"mark\":\"\"}}";

    public static RespQueryMdn parseResponseQueryMdn(String resStr) {
        RespQueryMdn resp = new RespQueryMdn();

        try {
            JSONObject obj = new JSONObject(resStr);
            int resCode = obj.optInt("resCode");
            LogUtils.d("parseResponseQueryMdn: resCode = " + resCode);
            switch(resCode) {
                case REQ_QUERY_MDN_SUCCESS:
                    String resMsg = obj.optString("resMsg");
                    String dataStr = obj.optString("data");
                    LogUtils.d("resMsg: " + resMsg);
                    LogUtils.d("data: " + dataStr);
                    if (TextUtils.isEmpty(dataStr)) {
                        LogUtils.e("ERROR: parseResponseQueryMdn: data empty");
                        resp.errMsg = "RESPONSE DATA EMPTY";
                    } else {
                        JSONObject dataObj = new JSONObject(dataStr);
                        String status = dataObj.optString("status");
                        LogUtils.i("parseResponseQueryMdn: status = " + status);
                        if (TextUtils.equals(status, MDN_STATUS_ACTIVE)) {
                            String nodeUrl = dataObj.optString("url");
                            if (TextUtils.isEmpty(nodeUrl)) {
                                resp.errMsg = "URL EMPTY";
                            }
                            else {
                                resp.url = nodeUrl;
                                resp.errCode = RESP_ERR_SUCCESS;
                            }
                        }
                        else {
                            resp.errMsg = "对不起，该手机号码业务已取消";
                        }
                    }
                    break;
                case REQ_QUERY_MDN_ERROR:
                    resp.errMsg = "QUERY ERROR";
                    break;
                case REQ_QUERY_MDN_NOT_FOUND:
                    resp.errMsg = "对不起，该手机号码尚未开通该业务";
                    break;
                case REQ_SERVER_ERROR:
                    resp.errMsg = "SERVER ERROR";
                    break;
                default:
                    LogUtils.e("ERROR: parseResponseQueryMdn: Unknown resCode " + resCode);
                    resp.errMsg = "";
                    break;
            }
        } catch (Exception e) {
            LogUtils.e("parseResponseQueryMdn Exception: " + e.toString());
            e.printStackTrace();
        }

        return resp;
    }
}
