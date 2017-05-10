package com.test.mulbrocasttest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class UnickSocketMng {

    private static final CommonLog log = LogFactory.createLog();

    private final static int RECDATA_MSG_ID = 0x0001;

    private DatagramSocket mDatagramSocket;
    private int mLocalPort;
    private boolean isInit = false;

    private HandlerThread mHandlerThread;
    private Handler mRecHandler;

    public UnickSocketMng() {

    }

    public boolean openSokcet(int localPort) {
        if (isInit) {
            return true;
        }

        try {
            mLocalPort = localPort;
            mDatagramSocket = new DatagramSocket(mLocalPort);
            isInit = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isInit;
    }

    public void closeSocket() {
        if (!isInit) {
            return;
        }

        try {
            mDatagramSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopListenThead();
        isInit = false;
    }

    public boolean syncSendData(String data, String targetIP, int targetPort) {
        if (!isInit) {
            return false;
        }

        SendTask task = new SendTask(data, targetIP, targetPort);
        Thread thread = new Thread(task);
        thread.start();

        return true;
    }

    public boolean startListenThread() {
        if (!isInit) {
            return false;
        }

        if (mHandlerThread == null) {
            startRevThread();
            mRecHandler.sendEmptyMessage(RECDATA_MSG_ID);
        }

        return true;
    }

    public void stopListenThead() {
        closeRecThread();
    }

    private class SendTask implements Runnable {

        private String mData;
        private String mTargetIP;
        private int mTargetPort;

        public SendTask(String data, String targetIP, int targetPort) {
            mData = data;
            mTargetIP = targetIP;
            mTargetPort = targetPort;
        }

        @Override
        public void run() {
            try {
                byte[] data = mData.getBytes("utf-8");
                InetAddress targetAddress = InetAddress.getByName(mTargetIP);
                DatagramPacket outPacket = new DatagramPacket(data, data.length, targetAddress, mTargetPort);

                log.e("UnicSocket sendData mTargetIP = " + mTargetIP + ", TARGET_PORT = " + mTargetPort
                        + "\ncontent --> " + mData);
                mDatagramSocket.send(outPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private boolean startRevThread() {
        if (mHandlerThread != null) {
            return true;
        }

        mHandlerThread = new HandlerThread("");
        mHandlerThread.start();
        mRecHandler = new Handler(mHandlerThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case RECDATA_MSG_ID:
                    try {
                        while (true) {
                            revData();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

        };

        return true;
    }

    private void closeRecThread() {
        if (mHandlerThread == null) {
            return;
        }

        mHandlerThread.quit();
        mHandlerThread = null;
    }

    private boolean revData() throws IOException {

        byte[] receiveData = new byte[1024];
        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);

        log.e("UnickSocket  block to receive packet!!!ip/port =  "
                + mDatagramSocket.getLocalAddress().getHostAddress().toString() + "/" + mDatagramSocket.getLocalPort());

        mDatagramSocket.receive(packet);
        String packetIpAddress = packet.getAddress().toString();
        packetIpAddress = packetIpAddress.substring(1, packetIpAddress.length());

        log.e("UnickSocket rec packet from --> ip: " + packetIpAddress + ", port = " + packet.getPort());
        String content = new String(receiveData, "utf-8");
        content = content.trim();
        log.e("content -->  " + content);

        return true;
    }
}
