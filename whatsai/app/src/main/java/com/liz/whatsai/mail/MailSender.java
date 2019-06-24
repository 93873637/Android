package com.liz.whatsai.mail;

import com.liz.whatsai.utils.LogUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * MailSender:
 * Created by liz on 2019/2/12.
 */

public class MailSender {

    private static final String KEY_SMTP_HOST = "mail.smtp.host";
    private static final String KEY_SMTP_PORT = "mail.smtp.port";
    private static final String KEY_SMTP_AUTH = "mail.smtp.auth";

    private static final String DEFAULT_SMTP_SERVER_PORT = "25";

    private String mailServerHost;
    private String mailServerPort;
    private String fromAddress;
    private String toAddress;
    private String userName;
    private String password;
    private boolean validate;
    private String subject;
    private String content;
    private String[] attachFiles;

    public MailSender() {
        mailServerHost = "";
        mailServerPort = DEFAULT_SMTP_SERVER_PORT;
        fromAddress = "";
        toAddress = "";
        userName = "";
        password = "";
        validate = true;
        subject = "";
        content = "";
        attachFiles = null;
    }

    public boolean send() {
        MailAuthenticator authenticator = null;
        Properties pro = getProperties();
        if (isValidate()) {
            authenticator = new MailAuthenticator(getUserName(), getPassword());
        }
        Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
        try {
            Message mailMessage = new MimeMessage(sendMailSession);
            Address from = new InternetAddress(getFromAddress());
            mailMessage.setFrom(from);
            Address to = new InternetAddress(getToAddress());
            mailMessage.setRecipient(Message.RecipientType.TO, to);
            mailMessage.setSubject(getSubject());
            mailMessage.setSentDate(new Date());

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(getContent(), "text/html;charset=utf-8");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            if (attachFiles != null && attachFiles.length > 0) {
                for (int i=0; i<attachFiles.length; i++) {
                    MimeBodyPart bodyPart = new MimeBodyPart();
                    try {
                        bodyPart.attachFile(attachFiles[i]);
                    } catch (IOException e) {
                        LogUtils.e("ERROR: MailSender: attch file exception: " + e.toString());
                        e.printStackTrace();
                    }
                    multipart.addBodyPart(bodyPart);
                }
            }
            mailMessage.setContent(multipart);
            Transport.send(mailMessage);
            return true;
        } catch (MessagingException ex) {
            LogUtils.e("ERROR: MailSender: send mail exception: " + ex.toString());
            ex.printStackTrace();
        }
        return false;
    }

    public Properties getProperties() {
        Properties p = new Properties();
        p.put(KEY_SMTP_HOST, this.mailServerHost);
        p.put(KEY_SMTP_PORT, this.mailServerPort);
        p.put(KEY_SMTP_AUTH, validate?"true":"false");
        return p;
    }

    public String getMailServerHost() {
        return mailServerHost;
    }

    public void setMailServerHost(String mailServerHost) {
        this.mailServerHost = mailServerHost;
    }

    public String getMailServerPort() {
        return mailServerPort;
    }

    public void setMailServerPort(String mailServerPort) {
        this.mailServerPort = mailServerPort;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public String[] getAttachFiles() {
        return attachFiles;
    }

    public void setAttachFiles(String[] fileList) {
        this.attachFiles = fileList;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String textContent) {
        this.content = textContent;
    }

    public boolean sendTextMail() {
        // 判断是否需要身份认证
        MailAuthenticator authenticator = null;
        Properties pro = this.getProperties();
        if (this.isValidate()) {
            // 如果需要身份认证，则创建一个密码验证器
            authenticator = new MailAuthenticator(this.getUserName(), this.getPassword());
        }
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
        try {
            // 根据session创建一个邮件消息
            Message mailMessage = new MimeMessage(sendMailSession);
            // 创建邮件发送者地址
            Address from = new InternetAddress(this.getFromAddress());
            // 设置邮件消息的发送者
            mailMessage.setFrom(from);
            // 创建邮件的接收者地址，并设置到邮件消息中
            Address to = new InternetAddress(this.getToAddress());
            mailMessage.setRecipient(Message.RecipientType.TO, to);
            // 设置邮件消息的主题
            mailMessage.setSubject(this.getSubject());
            // 设置邮件消息发送的时间
            mailMessage.setSentDate(new Date());
            // 设置邮件消息的主要内容
            String mailContent = this.getContent();
            mailMessage.setText(mailContent);
            // 发送邮件
            Transport.send(mailMessage);
            return true;
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean sendTextAndFileMail(String[] filePath) {
        // 判断是否需要身份认证
        MailAuthenticator authenticator = null;
        Properties pro = this.getProperties();
        if (this.isValidate()) {
            // 如果需要身份认证，则创建一个密码验证器
            authenticator = new MailAuthenticator(this.getUserName(), this.getPassword());
        }
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
        try {
            // 根据session创建一个邮件消息
            Message mailMessage = new MimeMessage(sendMailSession);
            // 创建邮件发送者地址
            Address from = new InternetAddress(this.getFromAddress());
            // 设置邮件消息的发送者
            mailMessage.setFrom(from);
            // 创建邮件的接收者地址，并设置到邮件消息中
            Address to = new InternetAddress(this.getToAddress());
            mailMessage.setRecipient(Message.RecipientType.TO, to);
            // 设置邮件消息的主题
            mailMessage.setSubject(this.getSubject());
            // 设置邮件消息发送的时间
            mailMessage.setSentDate(new Date());

            // 添加附件体
            BodyPart messageBodyPart = new MimeBodyPart();
            // 设置邮件消息的主要内容
            messageBodyPart.setContent(this.getContent(),  "text/html;charset=utf-8");
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            for (int i = 0; i < filePath.length; i++) {
                MimeBodyPart bodyPart = new MimeBodyPart();
                try {
                    //绑定附件路径
                    bodyPart.attachFile(filePath[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                multipart.addBodyPart(bodyPart);
            }
            mailMessage.setContent(multipart);

            // 发送邮件
            Transport.send(mailMessage);
            return true;
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean sendHtmlMail() {
        // 判断是否需要身份认证
        MailAuthenticator authenticator = null;
        Properties pro = this.getProperties();
        //如果需要身份认证，则创建一个密码验证器
        if (this.isValidate()) {
            authenticator = new MailAuthenticator(this.getUserName(), this.getPassword());
        }
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
        try {
            // 根据session创建一个邮件消息
            Message mailMessage = new MimeMessage(sendMailSession);
            // 创建邮件发送者地址
            Address from = new InternetAddress(this.getFromAddress());
            // 设置邮件消息的发送者
            mailMessage.setFrom(from);
            // 创建邮件的接收者地址，并设置到邮件消息中
            Address to = new InternetAddress(this.getToAddress());
            // Message.RecipientType.TO属性表示接收者的类型为TO
            mailMessage.setRecipient(Message.RecipientType.TO, to);
            // 设置邮件消息的主题
            mailMessage.setSubject(this.getSubject());
            // 设置邮件消息发送的时间
            mailMessage.setSentDate(new Date());
            // MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
            Multipart mainPart = new MimeMultipart();
            // 创建一个包含HTML内容的MimeBodyPart
            BodyPart html = new MimeBodyPart();
            // 设置HTML内容
            html.setContent(this.getContent(), "text/html; charset=utf-8");
            mainPart.addBodyPart(html);
            // 将MiniMultipart对象设置为邮件内容
            mailMessage.setContent(mainPart);
            // 发送邮件
            Transport.send(mailMessage);
            return true;
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
