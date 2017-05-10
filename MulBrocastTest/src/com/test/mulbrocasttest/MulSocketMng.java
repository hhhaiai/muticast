package com.test.mulbrocasttest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class MulSocketMng {

    public static interface RecDataCallback {
        public void onDataReceive(String hostIP, int port, final String data);
    }

    private static final CommonLog log = LogFactory.createLog();

    private final static int RECDATA_MSG_ID = 0x0001;

    private MulticastSocket multicastSocket;
    private InetAddress mGroup;
    private int mLocalPort;
    private boolean isInit = false;

    private HandlerThread mHandlerThread;
    private Handler mRecHandler;

    private RecDataCallback mCallback;

    public MulSocketMng() {

    }

    public boolean openSokcet(String groupIP, int localPort) {
        if (isInit) {
            return true;
        }

        try {
            mLocalPort = localPort;
            multicastSocket = new MulticastSocket(mLocalPort);
            multicastSocket.setLoopbackMode(true);
            mGroup = InetAddress.getByName(groupIP);
            multicastSocket.joinGroup(mGroup);

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
            multicastSocket.leaveGroup(mGroup);
            multicastSocket.close();
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

    public boolean startListenThread(RecDataCallback callback) {
        if (!isInit) {
            return false;
        }

        if (mHandlerThread == null) {
            startRevThread();
            mRecHandler.sendEmptyMessage(RECDATA_MSG_ID);
            mCallback = callback;
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
                MulticastSocket socket = new MulticastSocket();
                log.e("syncSendData mTargetIP = " + mTargetIP + ", TARGET_PORT = " + mTargetPort + "\ncontent --> "
                        + mData);
                socket.send(outPacket);
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendUDP(String data, String ip, int port) {
            try {
                byte[] datas = data.getBytes("utf-8");
                InetAddress targetAddress = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(datas, datas.length, targetAddress, port);
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

        log.e("block to receive packet!!!groupIp = " + mGroup.getHostAddress() + ", port = " + mLocalPort);
        multicastSocket.receive(packet);
        String packetIpAddress = packet.getAddress().toString();
        packetIpAddress = packetIpAddress.substring(1, packetIpAddress.length());

        log.e("rec packet from --> ip: " + packetIpAddress + ", port = " + packet.getPort());
        String content = new String(receiveData, "utf-8");
        content = content.trim();
        log.e("content -->  " + content);
        if (mCallback != null) {
            mCallback.onDataReceive(packetIpAddress, packet.getPort(), content);
        }

        return true;
    }
}
