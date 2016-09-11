package de.michaelsoftware.android.Vision.tools.network;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Michael on 23.01.2016.
 * Wakes the server with a WOL Magic Package
 */
public class WOLHelper extends AsyncTask<String, Integer, Boolean> {
    public static final int PORT = 9;
    public String IP = "";
    public String MAC = "";

    public WOLHelper(String ip, String mac) {
        this.IP = ip;
        this.MAC = mac;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String ipStr = IP;
        String macStr = MAC;

        try {
            byte[] macBytes = getMacBytes(macStr);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipStr);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        }
        catch (Exception e) {
            System.exit(1);
        }

        return true;
    }

    protected void onPostExecute(Boolean bool) {

    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}
