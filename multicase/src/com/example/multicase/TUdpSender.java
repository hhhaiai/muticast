package com.example.multicase;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

public class TUdpSender {
    public TUdpSender(WifiManager AWifiManager) {
        wifiManager = AWifiManager;
    }

    public TUdpSender(Activity AContext) {
        wifiManager = (WifiManager) AContext.getSystemService(AContext.WIFI_SERVICE);
    }

    MulticastLock multicastLock;
    WifiManager wifiManager;

    // 单播发送
    public void DoSendMsg(String ARemoteIP, int ARemotePort, String AMsg) {
        DatagramSocket skt = null;
        allowMulticast();
        try {
            skt = new DatagramSocket();
            DatagramPacket sendPgk = new DatagramPacket(AMsg.getBytes(), AMsg.getBytes().length,
                    InetAddress.getByName(ARemoteIP), ARemotePort);
            skt.send(sendPgk);
        } catch (Exception er) {
            ;
        } finally {
            try {
                skt.close();
            } catch (Exception er) {
            }
            try {
                multicastLock.release();
            } catch (Exception er) {
            }
        }
    }

    // 组播发送
    public void DoSendGroupMsg(String AGroupIP, int ARemotePort, String AMsg) {
        allowMulticast();
        MulticastSocket skt = null;
        try {
            skt = new MulticastSocket(ARemotePort);
            InetAddress broadcastAddress = InetAddress.getByName(AGroupIP);
            skt.joinGroup(broadcastAddress);
            skt.setLoopbackMode(true);
            DatagramPacket sendPgk = new DatagramPacket(AMsg.getBytes(), AMsg.getBytes().length,
                    InetAddress.getByName(AGroupIP), ARemotePort);
            skt.send(sendPgk);
        } catch (Exception er) {
            er.printStackTrace();
        } finally {
            try {
                skt.close();
            } catch (Exception er) {
            }
            try {
                multicastLock.release();
            } catch (Exception er) {
            }
        }
    }

    private void allowMulticast() {
        // WifiManager
        // wifiManager=(WifiManager)Context.getSystemService(Activity.WIFI_SERVICE);
        try {
            multicastLock = wifiManager.createMulticastLock("multicast.test");
            multicastLock.acquire();
        } catch (Exception er) {
            ;
        }
    }

    public static void DoSendUdpMsg(Activity AContext, String ARemoteIP, int ARemotePort, String AMsg) {
        TUdpSender Audp = new TUdpSender(AContext);
        Audp.DoSendMsg(ARemoteIP, ARemotePort, AMsg);
        Audp = null;
    }

    public static void DoSendUdpGroupMsg(Activity AContext, String AGroupIP, int ARemotePort, String AMsg) {
        TUdpSender Audp = new TUdpSender(AContext);
        Audp.DoSendGroupMsg(AGroupIP, ARemotePort, AMsg);
        Audp = null;
    }
}
