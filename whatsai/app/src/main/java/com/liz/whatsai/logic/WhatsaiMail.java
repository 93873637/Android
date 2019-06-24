package com.liz.whatsai.logic;

import android.text.TextUtils;

import com.liz.whatsai.mail.MailSender;
import com.liz.whatsai.utils.LogUtils;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * WhatsaiMail:
 * Created by liz on 2019/2/12.
 * <p>
 * CloudMinds Mail:
 * pop.exmail.qq.com
 * smtp.exmail.qq.com
 */

public class WhatsaiMail {
    private static final String MAIL_SMTP_HOST = "smtp.sina.com";
    private static final String MAIL_FROM_ADDRESS = "nehzil@sina.com";
    private static final String MAIL_USERNAME = "nehzil@sina.com";
    private static final String MAIL_PASSWORD = "tomacucu02&";

    public static void testSendMail() {
        new Thread() {
            public void run() {
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.sina.com");
                Session session = Session.getInstance(props, null);
                try {
                    MimeMessage msg = new MimeMessage(session);
                    msg.setFrom("nehzil@sina.com");
                    msg.setRecipients(Message.RecipientType.TO, "tom.li@cloudminds.com");
                    msg.setSubject("this is title");
                    msg.setSentDate(new Date());
                    msg.setText("hello! this is my mail message");
                    Transport.send(msg, "nehzil@sina.com", "tomacucu02&");
                    LogUtils.d("send mail successfully");
                } catch (MessagingException mex) {
                    LogUtils.e("ERROR: send mail failed, ex=" + mex);
                }
            }
        }.start();
    }

    public static void sendMail(final String toAddress, final String title, final String content) {
        sendMail(toAddress, title, content, "");
    }

    private static void sendMail(final String toAddress, final String title, final String content, final String filePath) {
        String[] filePathList = null;
        if (!TextUtils.isEmpty(filePath)) {
            filePathList = new String[]{filePath};
        }
        sendMail(toAddress, title, content, filePathList);
    }

    private static void sendMail(final String toAddress, final String title, final String content, final String[] filePathList) {
        new Thread() {
            public void run() {
                LogUtils.d("WhatsaiMail.sendMail: run send mail toAddress=" + toAddress + ", title="
                        + title + ", content=" + content + ", filePathList=" + (filePathList==null?"null":filePathList.length));
                MailSender sender = new MailSender();
                sender.setMailServerHost(MAIL_SMTP_HOST);
                sender.setFromAddress(MAIL_FROM_ADDRESS);
                sender.setUserName(MAIL_USERNAME);
                sender.setPassword(MAIL_PASSWORD);
                sender.setToAddress(toAddress);
                sender.setSubject(title);
                sender.setContent(content);
                if (filePathList != null && filePathList.length > 0) {
                    LogUtils.d("WhatsaiMail.sendMail: attach files, number = " + filePathList.length);
                    sender.setAttachFiles(filePathList);
                }
                if (sender.send()) {
                    LogUtils.d("WhatsaiMail.sendMail: send mail successfully");
                }
                else {
                    LogUtils.e("ERROR: WhatsaiMail.sendMail: send mail failed.");
                }
            }
        }.start();
    }
}
