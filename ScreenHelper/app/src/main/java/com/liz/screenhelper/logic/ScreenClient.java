package com.liz.screenhelper.logic;

import android.graphics.Bitmap;

import com.liz.screenhelper.utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

@SuppressWarnings({"WeakerAccess, unused"})
public class ScreenClient {

    private Socket mClientSocket;

    public ScreenClient(Socket clientSocket) {
        mClientSocket = clientSocket;
    }

    public void updateClientSocket(Socket clientSocket) {
        if (mClientSocket != null) {
            try {
                mClientSocket.close();
            }
            catch (Exception e) {
                LogUtils.e("ERROR: ScreenClient: close client socket exception: " + e.toString());
            }
        }
        mClientSocket = clientSocket;
    }

    public void run() {
        new UDPClientSocket_thread().start();
    }

    public void send_data(byte[] data) {
    }

    class UDPClientSocket_thread extends Thread {

        private DatagramSocket mUDPSocket;

        @Override
        public void run() {
            LogUtils.cbLog("ScreenClient: UDPClientSocket_thread: run: Enter...");

            try {
                mUDPSocket = new DatagramSocket(ComDef.DEFAULT_SCREEN_CLIENT_PORT);
            } catch (Exception e) {
                LogUtils.e("ERROR: ScreenClient: create client socket exception: " + e.toString());
                return;
            }

            while (true) {
                Bitmap bmp = DataLogic.deQueueScreenImage();
                if (bmp == null) {
                    LogUtils.cbLog("ScreenServer: cont_send_by_tcp: dequeue image null");
                    break;
                }

                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, ComDef.JPEG_QUALITY, baos);
                    baos.write(ComDef.SCREEN_IMAGE_END_FLAG.getBytes());
                    baos.flush();
                    byte[] data = baos.toByteArray();
                    DatagramPacket dataPacket = new DatagramPacket(data, data.length, mClientSocket.getInetAddress(), ComDef.DEFAULT_SCREEN_CLIENT_PORT);
                    mUDPSocket.send(dataPacket);
                } catch (Exception e) {
                    LogUtils.cbLog("ERROR: ScreenServer： client socket recv failed, ex=" + e.toString());
                    e.printStackTrace();
                    break;
                }
            }

            try {
                if (mUDPSocket != null) {
                    mUDPSocket.close();
                }
            }
            catch (Exception ex) {
                LogUtils.cbLog("ERROR: close client socket exception " + ex.toString());
                ex.printStackTrace();
            }

            LogUtils.cbLog("ScreenClient: UDPClientSocket_thread: run: Exit.");
        }
    }  //UDPClientSocket_thread

    class TCPClientSocket_thread extends Thread {

        Socket mClientSocket;
        OutputStream mOutputStream;
        TCPClientSocket_thread(Socket clientSocket) {
            mClientSocket = clientSocket;
        }

        @Override
        public void run() {
            LogUtils.cbLog("ScreenServer: TCPClientSocket_thread: run: Enter...");

            //cont_send_on_request();
            //ont_send_by_tcp();

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

            LogUtils.cbLog("ScreenServer: TCPClientSocket_thread: run: Exit");
        }

        private int cont_send_by_tcp() {
            try {
                mOutputStream = mClientSocket.getOutputStream();
                if (mOutputStream == null) {
                    LogUtils.cbLog("ERROR: ScreenServer: cont_send_by_tcp: no output stream from client socket");
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
                    LogUtils.cbLog("ScreenServer: cont_send_by_tcp: dequeue image null");
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

        /*
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
        //*/
    }  //TCPClientSocket_thread
}
