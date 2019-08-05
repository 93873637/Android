package com.liz.screenhelper.logic;

import com.liz.androidutils.LogUtils;
import com.liz.androidutils.NetUtils;
import com.liz.screenhelper.app.ThisApp;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

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
        LogUtils.d("ScreenServer: ScreenServer");
    }

    // Singleton
    ///////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Class Body

    private int mServerPort = 0;
    private String mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;
    private Map<String, ScreenClient> mClientList;

    private void _start() {
        mServerPort = ComDef.DEFAULT_SCREEN_SERVER_PORT;
        mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;
        mClientList = new HashMap<>();
        new ServerSocket_thread().start();
    }

    private String _getServerInfo() {
        String serverAddr = NetUtils.getLocalIpAddress(ThisApp.getAppContext());
        String info = serverAddr + ":" + mServerPort + "  " + mServerState;
        if (mServerState.equals(ComDef.SCREEN_SERVER_STATE_LISTENING)) {
            info += "  Connection: " + mClientList.size();
        }
        return info;
    }

    private String _getState() {
        return mServerState;
    }

    private static boolean isNetworkAvailable() {
        return NetUtils.isWifiConnected(ThisApp.getAppContext());
    }

    private void onNewConnect(Socket clientSocket) {
        String addr = clientSocket.getInetAddress().toString();
        ScreenClient sc = mClientList.get(addr);
        if (sc != null) {
            sc.updateClientSocket(clientSocket);
        }
        else {
            ScreenClient scNew = new ScreenClient(clientSocket);
            scNew.run();
            mClientList.put(addr, scNew);
            LogUtils.d("ScreenServer: Get a new connection from " + addr
                    + ":" + clientSocket.getPort() + ", Client Number = " + mClientList.size());
        }
    }

    class ServerSocket_thread extends Thread {

        ServerSocket mServerSocket = null;

        private boolean openServerSocket() {
            if (mServerSocket == null) {
                LogUtils.d("ScreenServer: open server socket...");
                try {
                    mServerSocket = new ServerSocket(ComDef.DEFAULT_SCREEN_SERVER_PORT);
                    mServerSocket.setSoTimeout(ComDef.SCREEN_SERVER_LOOP_INTERVAL);
                } catch (Exception e) {
                    LogUtils.d("ERROR: ScreenServer: open server socket exception: " + e.toString());
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        private void closeServerSocket() {
            if (mServerSocket != null) {
                LogUtils.d("ScreenServer: close server socket...");
                try {
                    mServerSocket.close();
                    mServerSocket = null;
                } catch (Exception e) {
                    LogUtils.d("ERROR: ScreenServer: close server socket exception: " + e.toString());
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            mServerState = ComDef.SCREEN_SERVER_STATE_RUNNING;
            LogUtils.d("ScreenServer: ServerSocket_thread run, E...");

            while (true) {
                if (isNetworkAvailable()) {
                    if (!openServerSocket()) {
                        LogUtils.d("ERROR: ScreenServer: open server socket failed");
                        break;
                    }
                } else {
                    LogUtils.d("WARNING: ScreenServer: network unavailable, close server socket, sleep " +
                            ComDef.SCREEN_SERVER_LOOP_INTERVAL + "ms to retry...");
                    closeServerSocket();
                    mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;
                    try {
                        Thread.sleep(ComDef.SCREEN_SERVER_LOOP_INTERVAL);
                        continue;
                    } catch (Exception ex) {
                        LogUtils.d("ERROR: ScreenServer: thread sleep exception: " + ex.toString());
                        ex.printStackTrace();
                        break;
                    }
                }

                mServerState = ComDef.SCREEN_SERVER_STATE_LISTENING;
                try {
                    //LogUtils.d("ScreenServer: waiting for connect...");
                    Socket clientSocket = mServerSocket.accept();
                    onNewConnect(clientSocket);
                    //new TCPClientSocket_thread(clientSocket).start();
                } catch (SocketTimeoutException e) {
                    //LogUtils.d("ScreenServer: server socket timeout");
                } catch (Exception e) {
                    LogUtils.d("ERROR: ScreenServer: server socket exception: " + e.toString());
                    e.printStackTrace();
                    break;
                }
            } //while (true)

            closeServerSocket();
            mServerState = ComDef.SCREEN_SERVER_STATE_STOPPED;
            LogUtils.d("ScreenServer: ServerSocket_thread run, X.");
        } //public void run()
    } //class ServerSocket_thread

    // Class Body
    ///////////////////////////////////////////////////////////////////////////////////////////////
}
