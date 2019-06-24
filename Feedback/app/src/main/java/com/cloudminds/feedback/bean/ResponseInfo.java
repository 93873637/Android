package com.cloudminds.feedback.bean;

public class ResponseInfo {
    private int code;
    private int report_id;
    private String status;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getReport_id() {
        return report_id;
    }

    public void setReport_id(int report_id) {
        this.report_id = report_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ResponseInfo{" +
                "code=" + code +
                ", report_id=" + report_id +
                ", status='" + status + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
