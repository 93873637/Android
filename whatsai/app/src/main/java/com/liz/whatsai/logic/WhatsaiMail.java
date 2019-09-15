package com.liz.whatsai.logic;

import android.app.Activity;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.MailSender;

@SuppressWarnings("unused, WeakerAccess")
public class WhatsaiMail {

    private static MailSender mMailSender = null;

    public static void start(final Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                send(activity);
            }
        }).start();
    }

    private static void send(final Activity activity) {
        if (mMailSender == null) {
            mMailSender = new MailSender();
            mMailSender.fromAddress = ComDef.MAIL_FROM_ADDRESS;
            mMailSender.fromAccount = ComDef.MAIL_FROM_ACCOUNT;
            mMailSender.fromPassword = ComDef.MAIL_FROM_PASSWORD;
            mMailSender.toAddress = ComDef.MAIL_TO_ADDRESS;
            mMailSender.ccAddress = ComDef.MAIL_CC_ADDRESS;
            mMailSender.smtpHost = ComDef.MAIL_SMTP_HOST;
            mMailSender.addAttachFile(ComDef.CLOUD_FILE_PATH);
        }

        String msg;
        if (mMailSender.send()) {
            msg = "WhatsaiMail: mail sent successfully";
            LogUtils.d(msg);
        }
        else {
            msg = "WhatsaiMail: send failed, error = " + mMailSender.errMsg;
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
            LogUtils.d("WhatsaiMail: no context to toast");
        }
    }
}
