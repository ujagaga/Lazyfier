/**
 * Created by Rada Berar
 */
package com.radinaradionica.lazyfier;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

/* Base class which all activities inherit in order to share common routines */
public class CommonFunctions extends AppCompatActivity {

    public class hostInfo{
        public String IPAddr = "";
        public int Port = 0;
        public String Name = "";
        public String Password = "";
    }

    public static hostInfo common_ServerSetup;
    private final int SOCKET_TIMEOUT = 500;
    private Handler serverTimeoutHandler;
    private Boolean serverTimedOut = false;

    public void common_msg(String s, boolean shortToast){
        int durability = Toast.LENGTH_LONG;

        if(shortToast){
            durability = Toast.LENGTH_SHORT;
        }
        Toast.makeText(getApplicationContext(), s, durability).show();
    }

    public void common_msg(String s){
        common_msg(s, false);
    }

    /* Performs communication to selected server and returns
    /* server response or empty string if there is no server communication */
    public String common_queryHost(int portNumber, String msgToHost, int timeout){

        String msgFromHost;

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(common_ServerSetup.IPAddr, portNumber), timeout);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            writer.write(msgToHost, 0, msgToHost.length());
            writer.flush();

            msgFromHost = reader.readLine();

            socket.close();

        } catch (IOException e1) {

            msgFromHost = "";
        }

        return msgFromHost;
    }

    public String common_queryHost(int portNumber, String msgToHost){
        return common_queryHost(portNumber, msgToHost, SOCKET_TIMEOUT);
    }


    /* Receive a UDP beacon from present servers */
    public String common_listPresentServers() {

        String result = "";

        byte[] buffer = new byte[128];

        /* Receive response from maximum 10 servers but limited timeout */
        serverTimeoutHandler = new Handler();
        serverTimeoutHandler.postDelayed(serverPingResponseTimeout, 1500);
        serverTimedOut = false;


        int i;
        for(i = 0; (i < 10) && (!serverTimedOut); i++) {
            try {
                if(CommonData.ReceiveSocket == null){
                    CommonData.ReceiveSocket = new DatagramSocket(CommonData.BROADCAST_RCV_PORT);
                    CommonData.ReceiveSocket.setBroadcast(true); // Not needed?
                    CommonData.ReceiveSocket.setSoTimeout(SOCKET_TIMEOUT);
                }

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                CommonData.ReceiveSocket.receive(packet);

                    String serverResponse = new String(packet.getData());

                    if(serverResponse.contains("NAME:")){
                        String serverName = serverResponse.split(":")[1];

                        if(!result.contains(serverName)){
                            result += serverName + ": ";

                            String serverIP = new String(packet.getAddress().toString());

                            result += serverIP.replace("/", "") + ";";
                        }

                }else{
                    break;
                }

            } catch (Exception e) {
                break;
            }
        }

        return result;
    }

    Runnable serverPingResponseTimeout = new Runnable() {
        @Override public void run() {
            serverTimedOut = true;
            serverTimeoutHandler.removeCallbacks(serverPingResponseTimeout);
            serverTimeoutHandler = null;
        }
    };

    public boolean common_testServerSettings(){
        return (!((common_ServerSetup.Name == "") || (common_ServerSetup.IPAddr == "")));
    }

    public void common_readSavedSettings() {
        SharedPreferences prefs = getSharedPreferences("srv_prefs", CommonFunctions.MODE_PRIVATE);

        if(common_ServerSetup == null) {
            common_ServerSetup = new hostInfo();
        }

        common_ServerSetup.Port = prefs.getInt("host_port", 0);
        common_ServerSetup.Name = prefs.getString("host_name", "");
        common_ServerSetup.Password = prefs.getString("host_password", "");
        common_ServerSetup.IPAddr = prefs.getString("host_ip", "");

        CommonData.skinIdx = prefs.getInt("skin_id", 1);
        CommonData.btn_backgroundId = prefs.getInt("btn_backgroundId", 0);
        CommonData.btn_color = prefs.getString("btn_color", "#000000");

        CommonData.showDonate = prefs.getBoolean("show_donate_btn", true);
        CommonData.showRate = prefs.getBoolean("show_rate_btn", true);

        CommonData.startupCount = prefs.getInt("startup_count", 0);

        if(CommonData.showDonate || CommonData.showRate) {
            CommonData.startupCount++;
            if(CommonData.startupCount > 100){
                CommonData.startupCount = 1;
            }

            /* Save new startup count */
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("startup_count", CommonData.startupCount);
            editor.apply();
        }
    }
}
