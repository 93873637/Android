package com.liz.multidialer.net;

import android.content.Context;

import com.liz.androidutils.LogUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class FTPManager {

    private final static int FTP_CONNECT_TIMEOUT = 60 * 1000;  //unit by millisecond
    private final static int FTP_DATA_TIMEOUT = 90 * 1000;  //unit by millisecond

    private FTPClient ftpClient = null;
  
    public FTPManager() {  
        ftpClient = new FTPClient();
    }  
  
    public synchronized boolean openFTP(String serverAddress, int port) throws Exception {
        boolean result = false;

        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }

        ftpClient.setConnectTimeout(FTP_CONNECT_TIMEOUT);
        ftpClient.setDataTimeout(FTP_DATA_TIMEOUT);
        //ftpClient.setControlEncoding("utf-8"); 
        //ftpClient.sendCommand("quote PASV");
        ftpClient.connect(serverAddress, port);

        // Determine if a reply code is a positive completion response.
        // All codes beginning with a 2 are positive completion responses.
        // The FTP server will send a positive completion response on the final successful completion of a command.
        int replyCode = ftpClient.getReplyCode();
        if (FTPReply.isPositiveCompletion(replyCode)) {
            LogUtils.e("ftp connect ok, error=" + replyCode);
            if (ftpClient.login("username", "password")) {
                result = true;
                LogUtils.d("ftp login successfully.");
            }
            else {
                LogUtils.e("ftp login failed.");
            }
        }
        else {
            LogUtils.e("ftp connect failed, error=" + replyCode);
        }

        return result;
    }  

    //directly upload file to ftp server root
    //localFilePath: full path with file name, such as: /sdcard/logs/offline_log.rar
    public synchronized boolean uploadFile(final Context context, String localFilePath)
            throws Exception {  
    	
        File localFile = new File(localFilePath);
        if (!localFile.exists()) {  
            LogUtils.d("file not exist for upload");
            return false;  
        }

        long localSize = localFile.length();
        if (localSize == 0) {
            LogUtils.d("file empty for upload");
            return false;
        }

        String fileName = localFile.getName();
        LogUtils.d("get local file name=" + fileName + ", size=" + localSize);

        //NOTE: set passive mode first before list files in case "500 Illegal PORT command."
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        //check if file exist on server
        long serverSize = 0;
        FTPFile[] files = ftpClient.listFiles(fileName);
        if (files.length == 0) {
            LogUtils.d("no file exists on server");
            serverSize = 0;  
        } else {
            serverSize = files[0].getSize();
            LogUtils.i("find file \"" + files[0].getName() + "\" on server, size=" + serverSize);

            if (serverSize < localSize) {
                LogUtils.i("resume uploading from break point...");
            }
            else {
                LogUtils.i("client file changed, deleteFile server file to re-upload");
                if (ftpClient.deleteFile(fileName)) {
                    serverSize = 0;
                } else {
                    LogUtils.i("failed to deleteFile server file for re-upload.");
                    return false;
                }
            }
        }

        //start upload from server size to local size
        RandomAccessFile rafLocal = new RandomAccessFile(localFile, "r");
        long step = localSize / 100;
        long process = 0;  
        long currentSize = 0;  

        ftpClient.setRestartOffset(serverSize);

        //move to server point to start
        rafLocal.seek(serverSize);
        OutputStream output = ftpClient.appendFileStream(fileName);
        byte[] b = new byte[8192];
        int length = 0;  
        while ((length = rafLocal.read(b)) != -1) {
            output.write(b, 0, length);  
            currentSize = currentSize + length;  
            if (currentSize / step != process) {  
                process = currentSize / step;  
                if (process % 10 == 0) {  
                    LogUtils.d("uploading " + process);
                }
            }  
        }

        //close output and waiting for FTP Server return 226 Transfer complete
        //NOTE: FTP Server do close only after receiving InputStream close, so call output.close() first.
        LogUtils.d("upload over, flush output...");
        output.flush();
        LogUtils.d("close output...");
        output.close();
        LogUtils.d("close local read...");
        rafLocal.close();

        LogUtils.d("waiting for complete...");
        if (ftpClient.completePendingCommand()) {
            LogUtils.d("upload completed");
            return true;
        }
        else {
            LogUtils.d("upload failed to complete");
            return false;
        }
    }

    public void closeFTP() {
        try {
            ftpClient.logout();
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        } catch (Exception e) {
            LogUtils.e("ftp disconnect exception: " + e.toString());
        }
    }
}
