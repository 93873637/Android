package com.liz.screenhelper.logic;

import android.graphics.Bitmap;

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
    private static int mConnectionNumber = 0;

    public static void start() {
        mServerAddr = NetUtils.getLocalIpAddress(ThisApp.getAppContext());
        mServerPort = ComDef.DEFAULT_SCREEN_SERVER_PORT;
        mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;
        mConnectionNumber = 0;
        new ServerSocket_thread().start();
    }

    public static String getServerInfo() {
        String info = mServerAddr + ":" + mServerPort + "  " + mServerState;
        if (mServerState.equals(ComDef.SCREEN_SERVER_STATE_RUNNING)) {
            info += "  Connection: " + mConnectionNumber;
        }
        return info;
    }

    public static String getState() {
        return mServerState;
    }

    public static boolean needScreenImage() {
        return (mServerState.equals(ComDef.SCREEN_SERVER_STATE_RUNNING) && mConnectionNumber > 0);
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
                    new ClientSocket_thread(clientSocket).start();
                    mConnectionNumber++;
                    LogUtils.d("ScreenServer: Get a new connection, mConnectionNumber = " + mConnectionNumber);
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

        Socket mClientSocket;
        ClientSocket_thread(Socket clientSocket) {
            mClientSocket = clientSocket;
        }

        @Override
        public void run() {
            LogUtils.d("ScreenServer: ClientSocket_thread: run: Enter, mConnectionNumber = " + mConnectionNumber + "...");
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
                        //outputStream.write("this is an echo test".getBytes());
                        Bitmap bmp = DataLogic.deQueueScreenImage();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                    }
                }
                catch (Exception e) {
                    LogUtils.e("ERROR: client socket recv failed, ex=" + e.toString());
                    e.printStackTrace();
                    break;
                }
            }
            mConnectionNumber--;
            LogUtils.d("ScreenServer: ClientSocket_thread: run: Exit, mConnectionNumber = " + mConnectionNumber);
        }
    }  //ClientSocket_thread
}
