package com.liz.whatsai.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * MailAuthenticator:
 * Created by liz on 2019/2/12.
 */

public class MailAuthenticator extends Authenticator {

    private String username;
    private String password;

    public MailAuthenticator(){
    }

    MailAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // this method will be called inside Authenticator
    protected PasswordAuthentication getPasswordAuthentication(){
        return new PasswordAuthentication(username, password);
    }
}
