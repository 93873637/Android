package com.liz.androidutils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

@SuppressWarnings("unused, WeakerAccess")
public class MailSender {

    public static final boolean MAIL_DEBUG = true;

    public static final String PROP_MAIL_SMTP_AUTH = "mail.smtp.auth";
    public static final String PROP_MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    public static final String PROP_MAIL_SMTP_HOST = "mail.smtp.host";

    public static final String DEFAULT_MAIL_TRANSPORT_PROTOCOL = "smtp";
    public static final String DEFAULT_MAIL_SMTP_AUTH = "true";
    public static final String DEFAULT_MAIL_SUBJECT_CHARSET = "UTF-8";
    public static final String DEFAULT_MAIL_CONTENT_TYPE = "text/html;charset=UTF-8";

    //Params Mandatory
    public String fromAddress = "";
    public String fromAccount = "";
    public String fromPassword = "";
    public String toAddress = "";
    public String smtpHost = "";

    //Params Optional
    public String ccAddress = "";
    public String bccAddress = "";
    public String mailSubject = "";
    public String mailContent = "";
    private ArrayList<String> attachFiles = new ArrayList<>();

    public String errMsg = "";

    public void addAttachFile(String fileAbsolute) {
        attachFiles.add(fileAbsolute);
    }

    private void logMsg(String msg) {
        LogUtils.e("MailSender: " + msg);
    }

    private void setErrMsg(String msg) {
        errMsg = "MailSender: " + msg;
        LogUtils.e(errMsg);
    }

    public boolean send() {
        if (!checkParams()) {
            logMsg("send failed with invalid param.");
            return false;
        }
        try {
            Properties props = getMailProp();
            Session session = Session.getInstance(props);
            session.setDebug(MAIL_DEBUG);
            Message msg = getMimeMessage(session);
            Transport transport = session.getTransport();
            transport.connect(fromAccount, fromPassword);
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();
            return true;
        }
        catch (Exception e) {
            setErrMsg("send exception: " + e.toString());
            return false;
        }
    }

    public boolean checkParams() {
        if (TextUtils.isEmpty(fromAddress)) {
            setErrMsg("checkParams: no fromAddress.");
            return false;
        }
        if (TextUtils.isEmpty(fromAccount)) {
            setErrMsg("checkParams: no fromAccount.");
            return false;
        }
        if (TextUtils.isEmpty(fromPassword)) {
            setErrMsg("checkParams: no fromPassword.");
            return false;
        }
        if (TextUtils.isEmpty(toAddress) && TextUtils.isEmpty(ccAddress) && TextUtils.isEmpty(bccAddress)) {
            setErrMsg("checkParams: no address to send.");
            return false;
        }
        if (TextUtils.isEmpty(smtpHost)) {
            setErrMsg("checkParams: no smtpHost.");
            return false;
        }
        return true;
    }

    public Properties getMailProp() {
        Properties props = new Properties();
        props.setProperty(PROP_MAIL_TRANSPORT_PROTOCOL, DEFAULT_MAIL_TRANSPORT_PROTOCOL);
        props.setProperty(PROP_MAIL_SMTP_AUTH, DEFAULT_MAIL_SMTP_AUTH);
        props.setProperty(PROP_MAIL_SMTP_HOST, smtpHost);
        return props;
    }

    public MimeMessage getMimeMessage(Session session) throws Exception{
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromAddress));

        if (!TextUtils.isEmpty(toAddress)) {
            @SuppressWarnings("static-access")
            InternetAddress[] internetAddressTo = new InternetAddress().parse(toAddress);
            msg.setRecipients(MimeMessage.RecipientType.TO, internetAddressTo);
            //msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toAddress));
        }

        if (!TextUtils.isEmpty(ccAddress)) {
            @SuppressWarnings("static-access")
            InternetAddress[] internetAddressCC = new InternetAddress().parse(ccAddress);
            msg.setRecipients(MimeMessage.RecipientType.CC, internetAddressCC);
            //msg.setRecipient(MimeMessage.RecipientType.CC, new InternetAddress(ccAddress));
        }

        if (!TextUtils.isEmpty(bccAddress)) {
            @SuppressWarnings("static-access")
            InternetAddress[] internetAddressBCC = new InternetAddress().parse(bccAddress);
            msg.setRecipients(MimeMessage.RecipientType.BCC, internetAddressBCC);
            //msg.setRecipient(MimeMessage.RecipientType.BCC, new InternetAddress(bccAddress));
        }

        if (!TextUtils.isEmpty(mailSubject)) {
            msg.setSubject(mailSubject, DEFAULT_MAIL_SUBJECT_CHARSET);
        }

        MimeMultipart mm = new MimeMultipart();

        //add mail content
        MimeBodyPart textBody = new MimeBodyPart();
        textBody.setContent(mailContent, DEFAULT_MAIL_CONTENT_TYPE);
        mm.addBodyPart(textBody);

        //create and add all attach nodes
        if (attachFiles.size() > 0) {
            for (int i=0; i<attachFiles.size(); i++) {
                String attachFile = attachFiles.get(i);
                MimeBodyPart attachBody = new MimeBodyPart();
                DataHandler dh = new DataHandler(new FileDataSource(attachFile));
                attachBody.setDataHandler(dh);
                attachBody.setFileName(MimeUtility.encodeText(dh.getName()));
                mm.addBodyPart(attachBody);     // 如果有多个附件，可以创建多个多次添加
            }
            mm.setSubType("mixed");
        }

        //add mixed node as last mail body
        msg.setContent(mm);

        //use current time as send time, send immediately
        msg.setSentDate(new Date());
        return msg;
    }

    private void addPicBody() {
//        // 5. 创建图片"节点"
//        MimeBodyPart image = new MimeBodyPart();
//        DataHandler dh = new DataHandler(new FileDataSource("/storage/0CCD-50F4/0.sd/whatsai/test.jpg"));
//        image.setDataHandler(dh);
//        // 为"节点"设置一个唯一编号（在文本"节点"将引用该ID）
//        image.setContentID("mailTestPic");
//
//        // 6. 创建文本"节点"
//        MimeBodyPart text = new MimeBodyPart();
//        // 这里添加图片的方式是将整个图片包含到邮件内容中, 实际上也可以以 http 链接的形式添加网络图片
//        text.setContent("这是一张图片<br/><a href='http://www.cnblogs.com/ysocean/p/7666061.html'><img src='cid:mailTestPic'/></a>", "text/html;charset=UTF-8");
//
//        // 7. （文本+图片）设置 文本 和 图片"节点"的关系（将 文本 和 图片"节点"合成一个混合"节点"）
//        MimeMultipart mm_text_image = new MimeMultipart();
//        mm_text_image.addBodyPart(text);
//        mm_text_image.addBodyPart(image);
//        mm_text_image.setSubType("related");    // 关联关系
//
//        // 8. 将 文本+图片 的混合"节点"封装成一个普通"节点"
//        // 最终添加到邮件的 Content 是由多个 BodyPart 组成的 Multipart, 所以我们需要的是 BodyPart,
//        // 上面的 mailTestPic 并非 BodyPart, 所有要把 mm_text_image 封装成一个 BodyPart
//        MimeBodyPart text_image = new MimeBodyPart();
//        text_image.setContent(mm_text_image);
//        // 10. 设置（文本+图片）和 附件 的关系（合成一个大的混合"节点" / Multipart ）
//
//        MimeMultipart mm = new MimeMultipart();
//        mm.addBodyPart(text_image);
//        mm.addBodyPart(attachment);     // 如果有多个附件，可以创建多个多次添加
//        mm.setSubType("mixed");         // 混合关系
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // java main and test functions

    public static void main(String[] args) {
        //following functions can only run on Android, or you will get Stub error!
//        test_send_mail_content();
//        ms.sendMail("this is a mail 带附件！", "/storage/0CCD-50F4/0.sd/whatsai/whatsai.zip");
//        ms.sendMail("this is 邮件 带附件和图片！", "/storage/0CCD-50F4/0.sd/whatsai/whatsai.zip", "/storage/0CCD-50F4/0.sd/whatsai/test.jpg");
    }

    public static void test_send_mail_simple() {
        MailSender ms = new MailSender();
        ms.fromAddress = "nehzil@sina.com";
        ms.fromAccount = "nehzil@sina.com";
        ms.fromPassword = "cfd5f95327cea45a";
        ms.toAddress = "93873637@qq.com";
        ms.smtpHost = "smtp.sina.com.cn";
        ms.mailContent = "this is 简单的纯文本邮件！";
        if (ms.send()) {
            System.out.println("test_send_mail_simple: success!");
        }
        else {
            System.out.println("ERROR: test_send_mail_simple: send failed!");
        }
    }

    public static void test_send_mail_multiple() {
        MailSender ms = new MailSender();
        ms.fromAddress = "nehzil@sina.com";
        ms.fromAccount = "nehzil@sina.com";
        ms.fromPassword = "cfd5f95327cea45a";
        ms.toAddress = "tom.li@cloudminds.com,wx.tom.li@qq.com,13910115737@139.com";
        //ms.ccAddress = "wx.tom.li@qq.com;13910115737@139.com;";
        //ms.bccAddress = "wx.tom.li@qq.com;13910115737@139.com;tom.li@cloudminds.com;";
        ms.smtpHost = "smtp.sina.com.cn";
        ms.mailContent = "this is test_send_mail_multiple！";
        if (ms.send()) {
            System.out.println("test_send_mail_multiple: success!");
        }
        else {
            System.out.println("ERROR: test_send_mail_multiple: send failed!");
        }
    }

    public static void test_send_mail_multiple_cc() {
        MailSender ms = new MailSender();
        ms.fromAddress = "nehzil@sina.com";
        ms.fromAccount = "nehzil@sina.com";
        ms.fromPassword = "cfd5f95327cea45a";
        ms.toAddress = "93873637@qq.com";
        ms.ccAddress = "wx.tom.li@qq.com,tom.li@cloudminds.com,13910115737@139.com";
        ms.smtpHost = "smtp.sina.com.cn";
        ms.mailContent = "this is test_send_mail_multiple_cc！";
        if (ms.send()) {
            System.out.println("test_send_mail_multiple_cc: success!");
        }
        else {
            System.out.println("ERROR: test_send_mail_multiple_cc: send failed!");
        }
    }

    public static void test_send_mail_multiple_bcc() {
        MailSender ms = new MailSender();
        ms.fromAddress = "nehzil@sina.com";
        ms.fromAccount = "nehzil@sina.com";
        ms.fromPassword = "cfd5f95327cea45a";
        ms.toAddress = "tom.li@cloudminds.com";
        ms.bccAddress = "wx.tom.li@qq.com,13910115737@139.com,lizhen70@gmail.com";
        ms.smtpHost = "smtp.sina.com.cn";
        ms.mailContent = "this is test_send_mail_multiple_bcc！";
        if (ms.send()) {
            System.out.println("test_send_mail_multiple_bcc: success!");
        }
        else {
            System.out.println("ERROR: test_send_mail_multiple_bcc: send failed!");
        }
    }

    public static void test_send_mail_content_subject() {
        MailSender ms = new MailSender();
        ms.fromAddress = "nehzil@sina.com";
        ms.fromAccount = "nehzil@sina.com";
        ms.fromPassword = "cfd5f95327cea45a";
        ms.toAddress = "tom.li@cloudminds.com";
        ms.smtpHost = "smtp.sina.com.cn";
        ms.mailSubject = "this is 邮件主题";
        ms.mailContent = "this is 简单的纯文本邮件 with subject！";
        if (ms.send()) {
            System.out.println("test_send_mail_content_subject: success!");
        }
        else {
            System.out.println("ERROR: test_send_mail_content_subject: send failed!");
        }
    }

    public static void test_send_mail_content_subject_attach() {
        MailSender ms = new MailSender();
        ms.fromAddress = "nehzil@sina.com";
        ms.fromAccount = "nehzil@sina.com";
        ms.fromPassword = "cfd5f95327cea45a";
        ms.toAddress = "tom.li@cloudminds.com";
        ms.smtpHost = "smtp.sina.com.cn";
        ms.mailSubject = "this is my 邮件主题";
        ms.mailContent = "this is test_send_mail_content_subject_attach！";
        ms.addAttachFile( "/storage/0CCD-50F4/0.sd/whatsai/whatsai.zip");
        if (ms.send()) {
            System.out.println("test_send_mail_content_subject_attach: success!");
        }
        else {
            System.out.println("ERROR: test_send_mail_content_subject_attach: send failed!");
        }
    }
}
