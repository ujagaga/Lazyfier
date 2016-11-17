package com.radinaradionica.lazyfier;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class ServerDiscoveryService extends IntentService {

    private String ServerIPAddr = "";
    private String ServerName = "";

    private Handler serverTimeoutHandler;
    private Boolean serverTimedOut = false;


    public ServerDiscoveryService() {
        super("ServerDiscoveryService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        readSavedSettings();

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
                    CommonData.ReceiveSocket.setSoTimeout(1400);
                }

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                CommonData.ReceiveSocket.receive(packet);

                String serverResponse = new String(packet.getData());

                if(serverResponse.contains("NAME:")){

                    if(ServerName.compareTo(serverResponse.split(":")[1]) == 0)
                    {
                        String responseIPAddr = packet.getAddress().toString().replace("/", "");

                        if(responseIPAddr.compareTo(ServerIPAddr) != 0){
                            ServerIPAddr = responseIPAddr;
                            saveServerIp();
                        }
                    }
                }

            } catch (Exception e) {
                break;
            }
        }
    }


    @Override
    public void onDestroy() {

        // restart service in 60 seconds
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.set( alarm.RTC, System.currentTimeMillis() + (1000 * 60),
                PendingIntent.getService(this, 0, new Intent(this, ServerDiscoveryService.class), 0)
        );
    }

    private void readSavedSettings() {
        SharedPreferences prefs = getSharedPreferences("srv_prefs", CommonFunctions.MODE_PRIVATE);

        ServerName = prefs.getString("host_name", "");
        ServerIPAddr = prefs.getString("host_ip", "");

    }

    private void saveServerIp(){
        SharedPreferences prefs = getSharedPreferences("srv_prefs", CommonFunctions.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("host_ip", ServerIPAddr);

        editor.apply();
    }

    Runnable serverPingResponseTimeout = new Runnable() {
        @Override public void run() {
            serverTimedOut = true;
            serverTimeoutHandler.removeCallbacks(serverPingResponseTimeout);
            serverTimeoutHandler = null;
        }
    };

}
