package com.liz.whatsai.logic;

import android.app.Activity;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.MailSender;

@SuppressWarnings("unused, WeakerAccess")
public class WSMail {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static WhatsaiMailCallback mWhatsaiMailCallback = null;
    public interface WhatsaiMailCallback {
        void onSendMailSuccess(String fileAbsolute);
    }
    public static void setWhatsaiMailCallback(WhatsaiMailCallback callback) {
        mWhatsaiMailCallback = callback;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void start(final Activity activity, final String fileAbsolute) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                send(activity, fileAbsolute);
            }
        }).start();
    }

    private static void send(final Activity activity, final String fileAbsolute) {
        LogUtils.d("WSMail: send: activity = " + activity + ", fileAbsolute = " + fileAbsolute);

        MailSender mMailSender = new MailSender();
        mMailSender.fromAddress = ComDef.MAIL_FROM_ADDRESS;
        mMailSender.fromAccount = ComDef.MAIL_FROM_ACCOUNT;
        mMailSender.fromPassword = ComDef.MAIL_FROM_PASSWORD;
        mMailSender.toAddress = ComDef.MAIL_TO_ADDRESS;
        mMailSender.ccAddress = ComDef.MAIL_CC_ADDRESS;
        mMailSender.smtpHost = ComDef.MAIL_SMTP_HOST;
        mMailSender.addAttachFile(fileAbsolute);

        String msg;
        if (mMailSender.send()) {
            msg = "WSMail: mail sent successfully, attach = " + fileAbsolute;
            LogUtils.d(msg);
            mWhatsaiMailCallback.onSendMailSuccess(fileAbsolute);
        }
        else {
            msg = "WSMail: send mail with attach \"" + fileAbsolute + "\" failed, error = " + mMailSender.errMsg;
            LogUtils.e(msg);
        }

        //show toast message
        final String msgToast = msg;
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, msgToast, Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            LogUtils.d("WSMail: no context to toast");
        }
    }
}
