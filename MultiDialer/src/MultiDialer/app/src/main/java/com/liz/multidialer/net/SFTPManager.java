package com.liz.multidialer.net;

import android.annotation.SuppressLint;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.liz.androidutils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;


public class SFTPManager {
    private String host;
    private int port;
    private String username;
    private String password;
    private ChannelSftp sftp;
    private Session sshSession;

    public SFTPManager(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.sftp = null;
        this.sshSession = null;
    }

    public boolean connect() {
        JSch jsch = new JSch();
        try {
            sshSession = jsch.getSession(username, host, port);
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshConfig.put("PreferredAuthentications","password");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            if (channel == null) {
                LogUtils.e("channel connecting failed.");
                return false;
            }
            channel.connect();
            sftp = (ChannelSftp) channel;
            return true;
        } catch (JSchException e) {
            LogUtils.e("connect failed with JSchException, ex = " + e.toString());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            LogUtils.e("connect failed, ex = " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
            }
            sftp = null;
        }
        if (sshSession != null) {
            if (sshSession.isConnected()) {
                sshSession.disconnect();
            }
            sshSession = null;
        }
    }

    public boolean uploadFile(String remotePath, String remoteFileName,
                              String localPath, String localFileName) {
        FileInputStream in = null;
        try {
            createDir(remotePath);
            File file = new File(localPath + localFileName);
            in = new FileInputStream(file);
            SFTPMonitor monitor = new SFTPMonitor(file.length());
            sftp.put(in, file.getName(), monitor);
            return true;
        } catch (FileNotFoundException e) {
            LogUtils.e("uploadFile with FileNotFoundException, ex = " + e.toString());
            e.printStackTrace();
        } catch (SftpException e) {
            LogUtils.e("uploadFile with SftpException, ex = " + e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            LogUtils.e("uploadFile Exception, ex = " + e.toString());
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean bacthUploadFile(String remotePath, String localPath, boolean del) {
        try {
            File file = new File(localPath);
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()
                        && files[i].getName().indexOf("bak") == -1) {
                    synchronized(remotePath){
                        if (this.uploadFile(remotePath, files[i].getName(),
                                localPath, files[i].getName())
                                && del) {
                            deleteLocalFile(localPath + files[i].getName());
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }
        return false;
    }

    /**
     * 批量下载文件
     *
     * @param remotPath
     *            远程下载目录(以路径符号结束)
     * @param localPath
     *            本地保存目录(以路径符号结束)
     * @param fileFormat
     *            下载文件格式(以特定字符开头,为空不做检验)
     * @param del
     *            下载后是否删除sftp文件
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean batchDownLoadFile(String remotPath, String localPath,
                                     String fileFormat, boolean del) {
        try {
            connect();
            Vector v = listFiles(remotPath);
            if (v.size() > 0) {

                Iterator it = v.iterator();
                while (it.hasNext()) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) it.next();
                    String filename = entry.getFilename();
                    SftpATTRS attrs = entry.getAttrs();
                    if (!attrs.isDir()) {
                        if (fileFormat != null && !"".equals(fileFormat.trim())) {
                            if (filename.startsWith(fileFormat)) {
                                if (this.downloadFile(remotPath, filename,
                                        localPath, filename)
                                        && del) {
                                    deleteRemoteFile(remotPath, filename);
                                }
                            }
                        } else {
                            if (this.downloadFile(remotPath, filename,
                                    localPath, filename)
                                    && del) {
                                deleteRemoteFile(remotPath, filename);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }
        return false;
    }

    public boolean downloadFile(String remotePath, String remoteFileName,
                                String localPath, String localFileName) {
        try {
            sftp.cd(remotePath);
            File file = new File(localPath + localFileName);
            mkdirs(localPath + localFileName);
            sftp.get(remoteFileName, new FileOutputStream(file));
            return true;
        } catch (FileNotFoundException e) {
            LogUtils.e("downloadFile FileNotFoundException, ex = " + e.toString());
            e.printStackTrace();
        } catch (SftpException e) {
            LogUtils.e("downloadFile SftpException, ex = " + e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            LogUtils.e("downloadFile Exception, ex = " + e.toString());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteLocalFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        if (!file.isFile()) {
            return false;
        }
        return file.delete();
    }

    public void deleteRemoteFile(String directory, String fileName) {
        try {
            sftp.cd(directory);
            sftp.rm(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean createDir(String createpath) {
        try {
            if (isDirExist(createpath)) {
                this.sftp.cd(createpath);
                LogUtils.d(createpath);
                return true;
            }
            String pathArry[] = createpath.split("/");
            StringBuffer filePath = new StringBuffer("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path + "/");
                if (isDirExist(createpath)) {
                    sftp.cd(createpath);
                } else {
                    sftp.mkdir(createpath);
                    sftp.cd(createpath);
                }
            }
            this.sftp.cd(createpath);
            return true;
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断目录是否存在
     * @param directory
     * @return
     */
    @SuppressLint("DefaultLocale")
    public boolean isDirExist(String directory) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

    /**
     * 创建目录
     * @param path
     */
    public void mkdirs(String path) {
        File f = new File(path);
        String fs = f.getParent();
        f = new File(fs);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    @SuppressWarnings("rawtypes")
    public Vector listFiles(String dir) {
        try {
            return sftp.ls(dir);
        }
        catch (Exception e) {
            LogUtils.e("SFTP: list files of dir \"" + dir + "\" failed, ex = " + e.toString());
            return null;
        }
    }

    public void mv(String srcFilePath, String tarFilePath) {
        String cmd = "mv " + srcFilePath + " " + tarFilePath;
        exec(cmd);
    }

    public void exec(String command) {
        ChannelExec channelExec = null;
        try {
            Channel channel = sshSession.openChannel("exec");
            channelExec = (ChannelExec) channel;
            channelExec.setCommand(command);
            channelExec.connect();
        } catch (Exception e) {
            e.printStackTrace();
            channelExec = null;
        }finally {
            channelExec.disconnect();
        }
    }
}
