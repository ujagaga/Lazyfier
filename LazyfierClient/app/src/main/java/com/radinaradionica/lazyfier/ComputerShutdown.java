package com.radinaradionica.lazyfier;

import android.content.Intent;
import android.os.StrictMode;
import android.os.Bundle;

public class ComputerShutdown extends CommonFunctions {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        common_readSavedSettings();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        startService(new Intent(this, ServerDiscoveryService.class));

        if(!common_testServerSettings()){
            common_msg("Please configure server first");
            startActivity(new Intent(this, Settings.class));
            finish();
        }

        common_msg("Sending", true);

        sendMsgToHost("SHUT_DOWN");

        finish();

    }


    private void sendMsgToHost(String payload){
        String msgToHost = "";

        if(common_ServerSetup.Password != ""){
            msgToHost = common_ServerSetup.Password + " ";
        }

        msgToHost += payload;

        String msgFromHost = common_queryHost(common_ServerSetup.Port, msgToHost);
        if( !msgFromHost.contains("ACK")){
            if(msgFromHost == ""){
                msgFromHost = "No server response!";
            }

            common_msg("ERROR: " + msgFromHost);
        }
    }

}
