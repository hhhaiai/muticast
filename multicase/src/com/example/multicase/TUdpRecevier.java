package com.example.multicase;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class TUdpRecevier implements Runnable {
    //
    // private String AGroupIP = "239.192.152.163";
    // private int ARemotePort = 48809;

    private static final String BROADCAST_IP = "239.192.152.163"; // 组播地址
    public static final int BROADCAST_PORT = 48809; // 组播端口
    // private static final String BROADCAST_IP = "230.0.0.1"; // 组播地址
    // public static final int BROADCAST_PORT = 8800; // 组播端口
    private static final int DATA_LEN = 4096; // 最大数据包大小
    protected static final String TAG = "sanbo";
    private MulticastSocket socket = null; // MulticastSocket实例
    private InetAddress broadcastAddress = null; // Internet地址
    byte[] inBuff = new byte[DATA_LEN]; // 接收消息的数组
    private DatagramPacket inPacket = new DatagramPacket(inBuff, inBuff.length); // 准备接收数据的DatagramPacket对象

    public Boolean IsActived = true; // 接收状态
    // public Handler RecMsgHabndler ; //接收到消息后的通知句柄

    private void DoInit() {
        Log.i(TAG, "DoInit-->init");
        try {
            socket = new MulticastSocket(BROADCAST_PORT);
            broadcastAddress = InetAddress.getByName(BROADCAST_IP);
            socket.joinGroup(broadcastAddress);
            socket.setLoopbackMode(false);
        } catch (Exception er) {
        } finally {
        }
    }

    public void DoStart() {
        if (!IsActived) {
            try {
                DoInit();
                IsActived = true;
                new Thread(this).start();
            } catch (Exception er) {
                ;
            }
        }
    }

    public void DoStop() {
        Log.i(TAG, "DoStop-->stop");
        if (IsActived) {
            try {
                IsActived = false;
                socket.close();
                socket = null;
            } catch (Exception er) {
            }
        }
    }

    public void run() {
        Log.i(TAG, "runnable: " + IsActived);
        while (IsActived) {
            try {
                inBuff = new byte[DATA_LEN];
                inPacket = new DatagramPacket(inBuff, inBuff.length);
                Log.i(TAG, "run-->run  C ");
                // 读取Socket中的数据，读到的数据放在inPacket所封装的字节数组里
                socket.receive(inPacket);
                Log.i(TAG, "run-->run  D ");
                String ARecTxt = new String(inBuff, 0, inPacket.getLength());
                Log.i(TAG, "receiver msg:" + ARecTxt);
                try {
                    DoOnReceiveMsg(ARecTxt);
                } catch (Exception er) {
                    String Err = er.getMessage();
                    int ALen = Err.length();
                }
            } catch (Exception er) {
                break;
            }
        }
        // DoStop();
    }

    public void DoOnReceiveMsg(String ARecMsg) {
        Message msg = new Message();
        Bundle b = new Bundle();// 存放数据
        b.putString("RecMsg", ARecMsg);
        msg.setData(b);
        this.RecMsgHabndler.sendMessage(msg);
    }

    private Handler RecMsgHabndler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "HandleMessage-->handleMessage");
            Log.i(TAG, msg.getData().getString("RecMsg"));
        }
    };
}