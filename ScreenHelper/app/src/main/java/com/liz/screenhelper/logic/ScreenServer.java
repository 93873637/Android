package com.liz.screenhelper.logic;

import com.liz.screenhelper.app.ThisApp;
import com.liz.screenhelper.utils.LogUtils;
import com.liz.screenhelper.utils.NetUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ScreenServer {

    private static String mServerAddr = "";
    private static int mServerPort = 0;
    private static String mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;

    public static void start() {
        mServerAddr = NetUtils.getLocalIpAddress(ThisApp.getAppContext());
        mServerPort = ComDef.DEFAULT_SCREEN_SERVER_PORT;
        mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;
        new ServerSocket_thread().start();
    }

    public static String getServerInfo() {
        return mServerAddr + ":" + mServerPort + " " + mServerState;
    }

    static class ServerSocket_thread extends Thread {
        @Override
        public void run() {
            mServerState = ComDef.SCREEN_SERVER_STATE_RUNNING;
            LogUtils.d("ScreenServer: ServerSocket_thread run...");
            try {
                ServerSocket serverSocket = new ServerSocket(ComDef.DEFAULT_SCREEN_SERVER_PORT);
                while (true) {
                    LogUtils.d("ScreenServer: listen on " + ComDef.DEFAULT_SCREEN_SERVER_PORT);
                    Socket clientSocket = serverSocket.accept();
                    LogUtils.d("ScreenServer: get connect, start client thread...");
                    new ClientSocket_thread(clientSocket).start();
                }
            }
            catch (Exception e) {
                LogUtils.e("ERROR: ScreenServer: server socket failed with exception: " + e.toString());
                e.printStackTrace();
            }
            mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;
        }
    }

    static class ClientSocket_thread extends Thread {

        Socket mClientSocket = null;

        ClientSocket_thread(Socket clientSocket) {
            mClientSocket = clientSocket;
        }

        @Override
        public void run() {
            LogUtils.d("ScreenServer: ClientSocket_thread run...");
            while (true) {
                try {
                    InputStream inputstream = mClientSocket.getInputStream();
                    final byte[] buf = new byte[4096];

                    LogUtils.d("ScreenServer: read buf...");
                    final int ret = inputstream.read(buf);
                    if (ret < 0) {
                        LogUtils.d("ScreenServer: recv failed with ret = " + ret);
                        break;
                    }
                    else if (ret == 0) {
                        LogUtils.d("ScreenServer: recv zero, peer closed?");
                        break;

                    }
                    else {
                        LogUtils.d("ScreenServer: recv, bytes = " + ret);
                        OutputStream outputStream = mClientSocket.getOutputStream();
                        outputStream.write("aaaab".getBytes());
                    }
                }
                catch (Exception e) {
                    LogUtils.e("ERROR: client socket recv failed, ex=" + e.toString());
                    e.printStackTrace();
                    break;
                }
            }
        }
    }  //ClientSocket_thread
}
