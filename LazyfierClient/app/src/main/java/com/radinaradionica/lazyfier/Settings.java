package com.radinaradionica.lazyfier;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/* The setup activity */
public class Settings extends CommonFunctions implements View.OnClickListener{

    private ImageButton skinChangeBtn;
    private ImageButton btnBackgroundPreview;
    private TextView Headline;
    private LinearLayout buttonList;
    private RelativeLayout choiceList;
    private Button rescanBtn;

    private ImageView skin1;
    private ImageView skin2;
    private ImageView skin3;
    private ImageView skin4;
    private ImageView skin5;
    private ImageView skin6;
    private ImageView skin7;
    private ImageView skin8;
    private ImageView skin9;
    private ImageView skin10;
    private ImageView skin11;
    private ImageView skin12;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setTitle("Lazyfier Setup");

        CommonData.newSkinIdx = CommonData.skinIdx;

        choiceList = (RelativeLayout)findViewById(R.id.Layout_choice);
        choiceList.setVisibility(View.VISIBLE);

        buttonList = (LinearLayout)findViewById(R.id.Layout_buttonList);
        buttonList.setVisibility(View.GONE);

        Headline = (TextView) findViewById(R.id.textView_Servers);

        listPresentServers();

        rescanBtn = (Button)findViewById(R.id.button_rescan);
        rescanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listPresentServers();
            }
        });

        skin1 = (ImageView)findViewById(R.id.imageView1);
        skin1.setOnClickListener(this);
        skin2 = (ImageView)findViewById(R.id.imageView2);
        skin2.setOnClickListener(this);
        skin3 = (ImageView)findViewById(R.id.imageView3);
        skin3.setOnClickListener(this);
        skin4 = (ImageView)findViewById(R.id.imageView4);
        skin4.setOnClickListener(this);
        skin5 = (ImageView)findViewById(R.id.imageView5);
        skin5.setOnClickListener(this);
        skin6 = (ImageView)findViewById(R.id.imageView6);
        skin6.setOnClickListener(this);
        skin7 = (ImageView)findViewById(R.id.imageView7);
        skin7.setOnClickListener(this);
        skin8 = (ImageView)findViewById(R.id.imageView8);
        skin8.setOnClickListener(this);
        skin9 = (ImageView)findViewById(R.id.imageView9);
        skin9.setOnClickListener(this);
        skin10 = (ImageView)findViewById(R.id.imageView10);
        skin10.setOnClickListener(this);
        skin11 = (ImageView)findViewById(R.id.imageView11);
        skin11.setOnClickListener(this);
        skin12 = (ImageView)findViewById(R.id.imageView12);
        skin12.setOnClickListener(this);


        btnBackgroundPreview = (ImageButton)findViewById(R.id.imageButton_preview);
        setupPreviewButton();
        btnBackgroundPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonList.setVisibility(View.VISIBLE);
                choiceList.setVisibility(View.GONE);
            }
        });

        skinChangeBtn = (ImageButton)findViewById(R.id.button_change);

        if(CommonData.newSkinIdx == 0){
            skinChangeBtn.setBackgroundResource(R.mipmap.skin1);
        }else{
            skinChangeBtn.setBackgroundResource(R.mipmap.skin2);
        }

        skinChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonData.newSkinIdx++;
                if(CommonData.newSkinIdx > 1){
                    CommonData.newSkinIdx = 0;
                    skinChangeBtn.setBackgroundResource(R.mipmap.skin1);
                }else{
                    skinChangeBtn.setBackgroundResource(R.mipmap.skin2);
                }

                CommonData.skinSettingsChanged = true;
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case  R.id.imageView1:{
                CommonData.btn_backgroundId = 0;
                CommonData.btn_color = skin1.getContentDescription().toString();
                break;
            }

            case  R.id.imageView2:{
                CommonData.btn_backgroundId = 1;
                CommonData.btn_color = skin2.getContentDescription().toString();
                break;
            }

            case  R.id.imageView3:{
                CommonData.btn_backgroundId = 2;
                CommonData.btn_color = skin3.getContentDescription().toString();
                break;
            }

            case  R.id.imageView4:{
                CommonData.btn_backgroundId = 3;
                CommonData.btn_color = skin4.getContentDescription().toString();
                break;
            }

            case  R.id.imageView5:{
                CommonData.btn_backgroundId = 4;
                CommonData.btn_color = skin5.getContentDescription().toString();
                break;
            }

            case  R.id.imageView6:{
                CommonData.btn_backgroundId = 5;
                CommonData.btn_color = skin6.getContentDescription().toString();
                break;
            }

            case  R.id.imageView7:{
                CommonData.btn_backgroundId = 6;
                CommonData.btn_color = skin7.getContentDescription().toString();
                break;
            }

            case  R.id.imageView8:{
                CommonData.btn_backgroundId = 7;
                CommonData.btn_color = skin8.getContentDescription().toString();
                break;
            }

            case  R.id.imageView9:{
                CommonData.btn_backgroundId = 8;
                CommonData.btn_color = skin9.getContentDescription().toString();
                break;
            }

            case  R.id.imageView10:{
                CommonData.btn_backgroundId = 9;
                CommonData.btn_color = skin10.getContentDescription().toString();
                break;
            }

            case  R.id.imageView11:{
                CommonData.btn_backgroundId = 10;
                CommonData.btn_color = skin11.getContentDescription().toString();
                break;
            }

            case  R.id.imageView12:{
                CommonData.btn_backgroundId = 11;
                CommonData.btn_color = skin12.getContentDescription().toString();
                break;
            }

            default:
                break;
        }

        CommonData.skinSettingsChanged = true;
        setupPreviewButton();
        buttonList.setVisibility(View.GONE);
        choiceList.setVisibility(View.VISIBLE);

    }



    private Boolean listPresentServers(){

        String scanResult = common_listPresentServers();

        if(scanResult == ""){
            Headline.setText("No Lazyfier servers found.");
            return false;
        }else{
            final String[] servers = scanResult.split(";");

            final ListView serversList = (ListView) findViewById(R.id.listView_Servers);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, servers);

            serversList.setAdapter(adapter);

            serversList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    common_ServerSetup.Name =  servers[position].split(": ")[0];
                    common_ServerSetup.IPAddr =  servers[position].split(": ")[1];

                    Boolean hostAvailable = checkHostAvailability();

                    if(!hostAvailable){
                        /* Host not available, but try one more time */
                        hostAvailable = checkHostAvailability();
                    }

                    if(hostAvailable){
                        int passwordTestResult = testHostPassword(common_ServerSetup.Password);
                        if(passwordTestResult != 2){
                            saveServerSettings();
                        }else{
                            requestUserPassword();
                        }
                    }else{
                        common_msg("Host not available!");
                    }
                }
            });
        }

        return true;
    }

    private boolean checkHostAvailability(){

        for(int portIdx = 0; portIdx < CommonData.ValidPorts.length; portIdx++){
            if(checkIfServerAvailable(CommonData.ValidPorts[portIdx])){
                common_ServerSetup.Port =  CommonData.ValidPorts[portIdx];
                return true;
            }
        }
        return false;
    }

    private void requestUserPassword(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Settings.this);
        alertDialog.setTitle("PASSWORD");
//        alertDialog.setMessage("Enter Password");

        final EditText input = new EditText(Settings.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("OK",
            new DialogInterface.OnClickListener(){
                public void onClick (DialogInterface dialog,int which) {
                    // Write your code here to execute after dialog
                    common_ServerSetup.Password = input.getText().toString();

                    int passwordTestResult = testHostPassword(common_ServerSetup.Password);

                    if(passwordTestResult != 2){
                        saveServerSettings();
                    }else{
                        common_msg("Bad password");
                    }
                }
            });
        alertDialog.show();
    }

    private void setupPreviewButton(){
        switch(CommonData.btn_backgroundId){
            case 0:
                btnBackgroundPreview.setBackgroundResource(R.mipmap.bgnd1);
                break;
            case 1:
                btnBackgroundPreview.setBackgroundResource(R.mipmap.bgnd3);
                break;
            case 2:
                btnBackgroundPreview.setBackgroundResource(R.drawable.btn_background_3);
                break;
            case 3:
                btnBackgroundPreview.setBackgroundResource(R.drawable.btn_background_4);
                break;
            case 4:
                btnBackgroundPreview.setBackgroundResource(R.drawable.btn_background_5);
                break;
            case 5:
                btnBackgroundPreview.setBackgroundResource(R.drawable.btn_background_6);
                break;
            case 6:
                btnBackgroundPreview.setBackgroundResource(R.drawable.btn_background_7);
                break;
            case 7:
                btnBackgroundPreview.setBackgroundResource(R.drawable.btn_background_8);
                break;
            case 8:
                btnBackgroundPreview.setBackgroundResource(R.drawable.btn_background_9);
                break;
            case 9:
                btnBackgroundPreview.setBackgroundResource(R.drawable.btn_background_10);
                break;
            case 10:
                btnBackgroundPreview.setBackgroundResource(R.drawable.btn_background_11);
                break;
            default:
                btnBackgroundPreview.setBackgroundResource(R.drawable.btn_background_12);
                break;
        }
    }

    /*
    * Testing host password: Returns:
    * 0: password OK
    * 1: password not needed
    * 2: password needed but not OK
     */
    private int testHostPassword(String Pass){

        /* Test if password is needed */
        String msgFromHost = common_queryHost(common_ServerSetup.Port, "Pass_test");

        if(msgFromHost.contains("password")){
            /* Password is required */
            msgFromHost = common_queryHost(common_ServerSetup.Port, Pass + " Pass_test");

            if(msgFromHost.contains("password")) {
                /* Bad password */
                return 2;
            }
        }else{
            /* No password needed */
            return 1;
        }
        return 0;
    }

    /* Tries to communicate with a server on a given port */
    private boolean checkIfServerAvailable(int Port){

        String msgFromHost = common_queryHost(Port, "NAME");

        if(msgFromHost.contains(common_ServerSetup.Name.trim())){
            return true;
        }

        return false;
    }

    public void saveServerSettings(){
        SharedPreferences prefs = getSharedPreferences("srv_prefs", CommonFunctions.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("host_name", common_ServerSetup.Name);
        editor.putString("host_password", common_ServerSetup.Password);
        editor.putInt("host_port", common_ServerSetup.Port);
        editor.putString("host_ip", common_ServerSetup.IPAddr);

        editor.apply();

        finish();
    }

}
