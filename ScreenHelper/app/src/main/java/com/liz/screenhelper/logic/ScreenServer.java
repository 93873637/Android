package com.liz.screenhelper.logic;

import android.graphics.Bitmap;

import com.liz.screenhelper.app.ThisApp;
import com.liz.screenhelper.utils.LogUtils;
import com.liz.screenhelper.utils.NetUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("unused")
public class ScreenServer {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Interfaces

    public static void start() {
        mScreenServer._start();
    }

    public static String getServerInfo() {
        return mScreenServer._getServerInfo();
    }

    public static String getState() {
        return mScreenServer._getState();
    }

    // Interfaces
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Singleton

    private static ScreenServer mScreenServer = new ScreenServer();
    private ScreenServer() {
        LogUtils.cbLog("ScreenServer: ScreenServer");
    }

    // Singleton
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Class Body

    private String mServerAddr = "";
    private int mServerPort = 0;
    private String mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;
    private int mConnectionNumber = 0;

    private void _start() {
        mServerAddr = NetUtils.getLocalIpAddress(ThisApp.getAppContext());
        mServerPort = ComDef.DEFAULT_SCREEN_SERVER_PORT;
        mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;
        mConnectionNumber = 0;
        new ServerSocket_thread().start();
    }

    private String _getServerInfo() {
        String info = mServerAddr + ":" + mServerPort + "  " + mServerState;
        if (mServerState.equals(ComDef.SCREEN_SERVER_STATE_RUNNING)) {
            info += "  Connection: " + mConnectionNumber;
        }
        return info;
    }

    private String _getState() {
        return mServerState;
    }

    class ServerSocket_thread extends Thread {
        @Override
        public void run() {
            mServerState = ComDef.SCREEN_SERVER_STATE_RUNNING;
            LogUtils.cbLog("ScreenServer: ServerSocket_thread run...");
            try {
                ServerSocket serverSocket = new ServerSocket(ComDef.DEFAULT_SCREEN_SERVER_PORT);
                while (true) {
                    LogUtils.cbLog("ScreenServer: waiting for connect...");
                    Socket clientSocket = serverSocket.accept();
                    new ClientSocket_thread(clientSocket).start();
                    mConnectionNumber++;
                    LogUtils.cbLog("ScreenServer: Get a new connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + ", mConnectionNumber = " + mConnectionNumber);
                }
            }
            catch (Exception e) {
                LogUtils.cbLog("ERROR: ScreenServer: server socket failed with exception: " + e.toString());
                e.printStackTrace();
            }
            mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;
        }
    }

    class ClientSocket_thread extends Thread {

        Socket mClientSocket;
        OutputStream mOutputStream;
        ClientSocket_thread(Socket clientSocket) {
            mClientSocket = clientSocket;
        }

        @Override
        public void run() {
            LogUtils.cbLog("ScreenServer: ClientSocket_thread: run: Enter, mConnectionNumber = " + mConnectionNumber + "...");

            //cont_send_on_request();
            cont_send();

            try {
                if (mOutputStream != null) {
                    mOutputStream.close();
                }
                if (mClientSocket != null) {
                    mClientSocket.close();
                }
            }
            catch (Exception ex) {
                LogUtils.cbLog("ERROR: close client socket exception " + ex.toString());
                ex.printStackTrace();
            }

            mConnectionNumber--;
            LogUtils.cbLog("ScreenServer: ClientSocket_thread: run: Exit, mConnectionNumber = " + mConnectionNumber);
        }

        private int cont_send() {
            try {
                mOutputStream = mClientSocket.getOutputStream();
                if (mOutputStream == null) {
                    LogUtils.cbLog("ERROR: ScreenServer: cont_send: no output stream from client socket");
                    return -1;
                }
            } catch (Exception e) {
                LogUtils.cbLog("ERROR: ScreenServer: get client output stream failed, ex=" + e.toString());
                e.printStackTrace();
                return -2;
            }

            while (true) {
                Bitmap bmp = DataLogic.deQueueScreenImage();
                if (bmp == null) {
                    LogUtils.cbLog("ScreenServer: cont_send: dequeue image null");
                    return -3;
                }

                try {
                    bmp.compress(Bitmap.CompressFormat.JPEG, ComDef.JPEG_QUALITY, mOutputStream);
                    mOutputStream.write(ComDef.SCREEN_IMAGE_END_FLAG.getBytes());
                    mOutputStream.flush();
                } catch (Exception e) {
                    LogUtils.cbLog("ERROR: ScreenServer： client socket recv failed, ex=" + e.toString());
                    e.printStackTrace();
                    return -4;
                }
            }
        }

        private int cont_send_on_request() {
            while(true) {
                try {
                    InputStream inputstream = mClientSocket.getInputStream();
                    final byte[] buf = new byte[4096];

                    LogUtils.cbLog("ScreenServer: waiting for read...");
                    int ret = inputstream.read(buf);
                    if (ret < 0) {
                        LogUtils.cbLog("ScreenServer: read failed with ret = " + ret);
                        return -1;
                    } else if (ret == 0) {
                        LogUtils.cbLog("ScreenServer: read zero, peer closed?");
                        return 0;
                    } else {
                        LogUtils.cbLog("ScreenServer: read ok, bytes = " + ret);
                        OutputStream mOutputStream = mClientSocket.getOutputStream();
                        if (mOutputStream == null) {
                            LogUtils.cbLog("ERROR: ScreenServer: no output stream from client socket");
                            return -2;
                        } else {
                            //mOutputStream.write("this is an echo test".getBytes());
                            Bitmap bmp = DataLogic.deQueueScreenImage();
                            if (bmp == null) {
                                LogUtils.cbLog("ScreenServer: dequeue image null");
                                mOutputStream.write(ComDef.SCREEN_IMAGE_EMPTY_FLAG.getBytes());
                            } else {
                                bmp.compress(Bitmap.CompressFormat.JPEG, 80, mOutputStream);
                                mOutputStream.write(ComDef.SCREEN_IMAGE_END_FLAG.getBytes());
                            }
                            mOutputStream.flush();
                            return ret;
                        }
                    }
                } catch (Exception e) {
                    LogUtils.cbLog("ERROR: client socket recv failed, ex=" + e.toString());
                    e.printStackTrace();
                    return -3;
                }
            }
        }
    }  //ClientSocket_thread

    // Class Body
    ///////////////////////////////////////////////////////////////////////////////////////////////
}
