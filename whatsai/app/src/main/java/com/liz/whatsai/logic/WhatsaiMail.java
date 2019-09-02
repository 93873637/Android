package com.liz.whatsai.logic;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.MailSender;

@SuppressWarnings("unused, WeakerAccess")
public class WhatsaiMail {

    private static MailSender mMailSender = null;

    public static void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                send();
            }
        }).start();
    }

    public static void send() {
        if (mMailSender == null) {
            mMailSender = new MailSender();
            mMailSender.fromAddress = ComDef.MAIL_FROM_ADDRESS;
            mMailSender.fromAccount = ComDef.MAIL_FROM_ACCOUNT;
            mMailSender.fromPassword = ComDef.MAIL_FROM_PASSWORD;
            mMailSender.toAddress = ComDef.MAIL_TO_ADDRESS;
            mMailSender.smtpHost = ComDef.MAIL_SMTP_HOST;
            mMailSender.addAttachFile(ComDef.MAIL_ATTACH_FILE_PATH);
        }
        if (mMailSender.send()) {
            LogUtils.d("WhatsaiMail: send mail ok");
        }
        else {
            LogUtils.e("WhatsaiMail: send mail failed");
        }
    }
}
