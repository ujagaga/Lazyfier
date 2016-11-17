package com.radinaradionica.lazyfier;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/* Main activity */
public class Lazyfier extends CommonFunctions implements View.OnTouchListener, View.OnLongClickListener {

    private ImageButton btnShutDown;
    private ImageButton btnStop;
    private ImageButton btnPlay;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private ImageButton btnVolUp;
    private ImageButton btnVolDown;
    private ImageButton btnMsg;
    private ImageButton btnMouse;
    GridLayout buttonGrid;
    FrameLayout playBtnFrame;
    private final int DEFAULT_WINDOW_REFRESH_TIMEOUT = 2000;
    private int windowRefreshTimeout = 2000;

    /* Class used to store number of times a button was pressed so we can send
    * server commands asynchronously, not blocking the app UI, while still be able
    * to press a button and send commands later when all other communication is done
    * */
    private class buttonPress{
        public Integer volUp = 0;
        public Integer volDown = 0;
        public Integer stop = 0;
        public Integer play = 0;
        public Integer next = 0;
        public Integer prev = 0;
        public Integer forward = 0;
        public Integer rewind = 0;
        public Integer shutDown = 0;
        public Integer space = 0;
        public Integer close = 0;
        public Integer fullscr = 0;
    }
    
    private class Flags {
        public Boolean playBtnLong = false;
        public Boolean shutdownBtnLong = false;
        public Boolean stopBtnLong = false;
        public Boolean nextBtnLong = false;
        public Boolean prevBtnLong = false;

        public Boolean serverError = false;
        public Boolean txInProgress = false;
        public Boolean specialWindowButtonsActive = false;
    }

    private buttonPress btnPressCount;
    private Flags flags;  
    private final String[] BlackListedTitles = {"Desktop", "Program Manager"};
    private String specialWindowName;
    private int maxTitleIndex = 0;
    private int currentTitleIndex = 0;
    private int numberOfServerErrors = 0;
    private Handler getActiveWindowHandler = new Handler();
    private Handler commandRepeatHandler;
    Animation animScale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonData.skinSettingsChanged = false;

        common_readSavedSettings();

        if(CommonData.skinIdx == 0){
            setContentView(R.layout.activity_lazyfier_1);
        }else {
            setContentView(R.layout.activity_lazyfier_2);
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(CommonData.btn_color)));

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        startService(new Intent(this, ServerDiscoveryService.class));

        if(!common_testServerSettings()){
            Intent settingsIntent = new Intent(Lazyfier.this, Settings.class);
            Lazyfier.this.startActivityForResult(settingsIntent, 0);
        }

        animScale = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        btnPressCount = new buttonPress();
        flags = new Flags();

        btnNext = (ImageButton)findViewById(R.id.imageButton_next);
        btnPrev = (ImageButton)findViewById(R.id.imageButton_prev);
        btnPlay = (ImageButton)findViewById(R.id.imageButton_play);
        btnShutDown = (ImageButton)findViewById(R.id.imageButton_shutdown);
        btnStop = (ImageButton)findViewById(R.id.imageButton_stop);
        btnVolDown = (ImageButton)findViewById(R.id.imageButton_vol_down);
        btnVolUp = (ImageButton)findViewById(R.id.imageButton_vol_up);
        btnMsg = (ImageButton)findViewById(R.id.button_msg);
        btnMouse = (ImageButton)findViewById(R.id.button_cursor);
        buttonGrid = (GridLayout)findViewById(R.id.buttonGrid);
        playBtnFrame = (FrameLayout)findViewById(R.id.playBtnFrame);

        btnPlay.setOnTouchListener(this);
        btnPlay.setOnLongClickListener(this);
        btnStop.setOnTouchListener(this);
        btnStop.setOnLongClickListener(this);
        btnShutDown.setOnTouchListener(this);
        btnShutDown.setOnLongClickListener(this);
        btnNext.setOnTouchListener(this);
        btnPrev.setOnTouchListener(this);
        btnVolUp.setOnTouchListener(this);
        btnVolDown.setOnTouchListener(this);

        setButtonBackgrounds();

        btnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AskForMsg();
            }
        });

        btnMouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cursorIntent = new Intent(Lazyfier.this, Cursor.class);
                Lazyfier.this.startActivity(cursorIntent);
            }
        });

        if((CommonData.startupCount == 50) && CommonData.showDonate ){
            AskForDonation();
        }

        if ((CommonData.startupCount == 30) && CommonData.showRate) {
            AskToRateOnPlayStore();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                v.setAlpha(0.2f);
                v.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                v.setAlpha(1);
                v.invalidate();
                break;
            }
        }

        switch (v.getId()){
            case R.id.imageButton_play: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        flags.playBtnLong = false;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if(!flags.playBtnLong){
                            btnPressCount.play++;
                            sendQueuedCommand();
                        }
                        break;
                    }
                    default:
                        break;
                }
                break;
            }

            case R.id.imageButton_stop: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        flags.stopBtnLong = false;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if(!flags.stopBtnLong) {
                            if (flags.specialWindowButtonsActive) {
                                btnPressCount.fullscr++;
                            } else {
                                btnPressCount.stop++;
                            }
                            sendQueuedCommand();
                        }
                        break;
                    }
                    default:
                        break;
                }
                break;
            }

            case R.id.imageButton_shutdown: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        flags.shutdownBtnLong = false;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if(!flags.shutdownBtnLong){
                            btnPressCount.close++;
                            sendQueuedCommand();
                        }
                        break;
                    }
                    default:
                        break;
                }
                break;
            }

            case R.id.imageButton_next: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        flags.nextBtnLong = false;

                        /* Start timer for repeating  fast forward command while button is held down */
                        if (commandRepeatHandler == null) {
                            commandRepeatHandler = new Handler();
                            commandRepeatHandler.postDelayed(repeatFastForward, 1000);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if (commandRepeatHandler != null) {
                            commandRepeatHandler.removeCallbacks(repeatFastForward);
                            commandRepeatHandler = null;
                        }

                        if(!flags.nextBtnLong){
                            if (flags.specialWindowButtonsActive){
                                btnPressCount.forward++;
                            }else{
                                btnPressCount.next++;
                            }
                            sendQueuedCommand();
                        }
                        break;
                    }
                    default:
                        break;
                }
                break;
            }

            case R.id.imageButton_prev: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        flags.prevBtnLong = false;

                        /* Start timer for repeating  fast forward command while button is held down */
                        if (commandRepeatHandler == null) {
                            commandRepeatHandler = new Handler();
                            commandRepeatHandler.postDelayed(repeatRewind, 1000);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if (commandRepeatHandler != null) {
                            commandRepeatHandler.removeCallbacks(repeatRewind);
                            commandRepeatHandler = null;
                        }

                        if(!flags.prevBtnLong){
                            if (flags.specialWindowButtonsActive){
                                btnPressCount.rewind++;
                            }else{
                                btnPressCount.prev++;
                            }
                            sendQueuedCommand();
                        }
                        break;
                    }
                    default:
                        break;
                }
                break;
            }

            case R.id.imageButton_vol_up: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        btnPressCount.volUp++;
                        sendQueuedCommand();
                        break;
                    }
                    default:
                        break;
                }
                break;
            }

            case R.id.imageButton_vol_down: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        btnPressCount.volDown++;
                        sendQueuedCommand();
                        break;
                    }
                    default:
                        break;
                }
                break;
            }

            default:
                break;
        }

        return false;

    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()){
            case R.id.imageButton_play: {
                flags.playBtnLong = true;
                btnPressCount.space++;
                sendQueuedCommand();
                v.startAnimation(animScale);
                break;
            }

            case R.id.imageButton_stop: {
                if(flags.specialWindowButtonsActive){
                    btnPressCount.stop++;
                }else{
                    btnPressCount.fullscr++;
                }
                sendQueuedCommand();
                v.startAnimation(animScale);
                break;
            }

            case R.id.imageButton_shutdown: {
                flags.shutdownBtnLong = true;
                btnPressCount.shutDown++;
                sendQueuedCommand();
                v.startAnimation(animScale);
                break;
            }

            default:
                break;
        }

        return true;    // <- set to true
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.settings:
                Intent settingsIntent = new Intent(Lazyfier.this, Settings.class);
                Lazyfier.this.startActivityForResult(settingsIntent, 0);
                break;

            case R.id.about:
                showAbout();
                break;
        }
        return true;
    }

    @Override
    protected void onPause()
    {
        getActiveWindowHandler.removeCallbacks(refreshActiveWindow);
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        refreshActiveWindow.run();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    /* Called after closing settings activity */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        setTitle("Lazyfier");

        if(!common_testServerSettings()){
            common_msg("Sorry, no server detected. \nExiting Lazyfier!");
            new CountDownTimer(3000, 3000) {
                public void onTick(long millisUntilFinished) {

                }
                public void onFinish() {
                    finish();
                }
            }.start();
        }else{

            if(CommonData.skinSettingsChanged){
                CommonData.skinIdx = CommonData.newSkinIdx;
                CommonData.skinSettingsChanged = false;
                saveSkinSettings();

                /* Restart App */
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        }
    }



    private String sendMsgToHost(String payload, int timeout){
        String msgToHost = "";

        if(common_ServerSetup.Password != ""){
            msgToHost = common_ServerSetup.Password + " ";
        }

        msgToHost += payload;

        String msgFromHost = common_queryHost(common_ServerSetup.Port, msgToHost, timeout);
        if( !msgFromHost.contains("ACK")){
            if(msgFromHost == ""){
                msgFromHost = "No server response!";
            }

            btnPressCount.next = 0;
            btnPressCount.prev = 0;
            btnPressCount.forward = 0;
            btnPressCount.rewind = 0;
            btnPressCount.play = 0;
            btnPressCount.shutDown = 0;
            btnPressCount.volDown = 0;
            btnPressCount.volUp = 0;
            btnPressCount.stop = 0;
            btnPressCount.space = 0;
            btnPressCount.fullscr = 0;
            btnPressCount.close = 0;

            return("ERROR: " + msgFromHost);
        }

        return("");
    }

    private String sendMsgToHost(String payload){
        return sendMsgToHost(payload, 1000);
    }

    /* Displays a popup dialog asking the user to input the message which is to be sent
    * to the server to popup on the computer screen
    * */
    private void AskForMsg(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Lazyfier.this);
        alertDialog.setTitle("Message");

        final EditText input = new EditText(Lazyfier.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        alertDialog.setView(input);

        alertDialog.setPositiveButton("Send",
                new DialogInterface.OnClickListener(){
                    public void onClick (DialogInterface dialog,int which) {
                        // Write your code here to execute after dialog
                        String result = sendMsgToHost("MSG:" + input.getText().toString());
                        if(result != "") {
                            common_msg(result);
                        }
                    }
                });

        input.requestFocus();
        alertDialog.show();
    }

    /* Sending commands to the server must be asynchronous so we do not block
    * the application in case of a longer timeout.
    * */
    private class AsyncSend extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            while(flags.txInProgress) {

                String payload;
                Boolean repeating = false;

                if(btnPressCount.volDown > 0){
                    btnPressCount.volDown--;
                    payload = "VOL_DOWN";
                }else if(btnPressCount.volUp > 0){
                    btnPressCount.volUp--;
                    payload = "VOL_UP";
                }else if(btnPressCount.play > 0){
                    btnPressCount.play = 0;
                    payload = "PLAY";
                }else if(btnPressCount.stop > 0){
                    btnPressCount.stop = 0;
                    payload = "STOP";
                }else if(btnPressCount.shutDown > 0){
                    btnPressCount.shutDown = 0;
                    payload = "SHUT_DOWN";
                }else if(btnPressCount.next > 0){
                    btnPressCount.next = 0;
                    payload = "NEXT";
                }else if(btnPressCount.prev > 0) {
                    btnPressCount.prev = 0;
                    payload = "PREV";
                }else if(btnPressCount.forward > 0){
                    btnPressCount.forward--;
                    payload = "FORWARD";
                    repeating = true;
                }else if(btnPressCount.rewind > 0) {
                    btnPressCount.rewind--;
                    payload = "REWIND";
                    repeating = true;
                }else if(btnPressCount.space > 0){
                    btnPressCount.space = 0;
                    payload = "SPACE";
                }else if(btnPressCount.close > 0){
                    btnPressCount.close = 0;
                    payload = "CLOSE";
                }else if(btnPressCount.fullscr > 0){
                    btnPressCount.fullscr = 0;
                    payload = "FULLSCR";
                }else{
                    flags.txInProgress = false;
                    break;
                }

                String result;

                if(!repeating) {
                    result = sendMsgToHost(payload);
                    if(result != ""){
                    /* Error, but try one more time */
                        result = sendMsgToHost(payload);
                    }
                }else {
                    /* This one is time limited, so no retries */
                    result = sendMsgToHost(payload, 450);
                }

                if(result != ""){
                    return result;
                }
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if((btnPressCount.volUp == 0) && (btnPressCount.volDown == 0) &&
                    (btnPressCount.next == 0) && (btnPressCount.prev == 0) &&
                    (btnPressCount.play == 0) && (btnPressCount.stop == 0) &&
                    (btnPressCount.shutDown == 0)){
                flags.txInProgress = false;
            }

            if(result != "") {
                if(!flags.serverError) {
                    flags.serverError = true;
                    common_msg(result);

                    /* Prevent multiple server error messages */
                    new CountDownTimer(3000, 3000) {
                        public void onTick(long millisUntilFinished) {

                        }
                        public void onFinish() {
                            flags.serverError = false;
                        }
                    }.start();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            flags.txInProgress = true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    /* Helper routine to start AsyncSend() which will keep going untill all commands are sent,
    * so there is no need to start it again untill it is finished
    * */
    private void sendQueuedCommand(){
        if(!flags.txInProgress){
            AsyncSend sendMsg = new AsyncSend();
            sendMsg.execute();
        }
    }

    private void showAbout(){
        final String aboutString = "Author: Rada Berar\ne-mail: ujagaga@gmail.com\n\nTo view details and optionally donate go to:\nhttp://radinaradionica.com/en/handicrafts/Programming";

        final SpannableString s = new SpannableString(aboutString);
        Linkify.addLinks(s, Linkify.ALL);


        final AlertDialog alertDialog = new AlertDialog.Builder(Lazyfier.this)
                .setPositiveButton("Dismiss", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage( s )
                .setTitle("About")
                .create();

        alertDialog.show();

        // Make the textview clickable. Must be called after show()
        ((TextView)alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    /* Timed process to refresh active window title */
    Runnable refreshActiveWindow = new Runnable()
    {
        @Override
        public void run() {

            String titleToSet = "";

            String response = sendMsgToHost("ACTIVE_WINDOW");
            if(response.contains("TITLE:")) {

                String[] responseList = response.split("TITLE:");

                if (responseList.length > 1) {
                    String receivedWindowTitle = responseList[1];

                    if(receivedWindowTitle.startsWith("Shutdown ")){
                        setTitle(receivedWindowTitle);
                        windowRefreshTimeout = 1000;
                    }else {
                        windowRefreshTimeout = DEFAULT_WINDOW_REFRESH_TIMEOUT;

                        Boolean titleBlackListed = false;

                        for (int i = 0; i < BlackListedTitles.length; i++) {
                            if (receivedWindowTitle.startsWith(BlackListedTitles[i])) {
                                titleBlackListed = true;
                                break;
                            }
                        }

                        if (!titleBlackListed) {
                            String[] titleList = receivedWindowTitle.split("-");

                            maxTitleIndex = titleList.length;

                            if (receivedWindowTitle.contains("- YouTube -")) {
                                specialWindowName = "youtube";
                            } else {
                                specialWindowName = "";
                            }

                            if (currentTitleIndex < maxTitleIndex) {
                                titleToSet = titleList[currentTitleIndex];
                            }
                        } else {
                            specialWindowName = "";
                        }

                        if (titleToSet == "") {
                            setTitle("Lazyfier");
                        } else {
                            setTitle(titleToSet);
                        }

                        currentTitleIndex++;
                        if (currentTitleIndex > maxTitleIndex) {
                            currentTitleIndex = 0;
                        }
                    }

                    numberOfServerErrors = 0;
                }else {
                    numberOfServerErrors++;
                }

            } else {
                numberOfServerErrors++;
            }

            if(numberOfServerErrors > 3){
                setTitle("Lazyfier");
            }

            if(specialWindowName == "youtube"){
                if(!flags.specialWindowButtonsActive){
                    btnStop.setImageResource(R.mipmap.fullscreen);
                    btnStop.setContentDescription(getResources().getString(R.string.btn_fullscreen));
                    btnPrev.setImageResource(R.mipmap.rwd);
                    btnPrev.setContentDescription(getResources().getString(R.string.btn_rewind));
                    btnNext.setImageResource(R.mipmap.fwd);
                    btnNext.setContentDescription(getResources().getString(R.string.btn_fast_forward));
                    flags.specialWindowButtonsActive = true;
                }

            }else{
                if(flags.specialWindowButtonsActive) {
                    btnStop.setImageResource(R.mipmap.stop);
                    btnStop.setContentDescription(getResources().getString(R.string.btn_stop));
                    btnPrev.setImageResource(R.mipmap.previous);
                    btnPrev.setContentDescription(getResources().getString(R.string.btn_previous));
                    btnNext.setImageResource(R.mipmap.next);
                    btnNext.setContentDescription(getResources().getString(R.string.btn_next));
                    flags.specialWindowButtonsActive = false;
                }

            }

            getActiveWindowHandler.postDelayed(refreshActiveWindow, windowRefreshTimeout);
        }
    };

    Runnable repeatFastForward = new Runnable() {
        @Override public void run() {

            btnPressCount.forward++;
            sendQueuedCommand();

            if(!flags.nextBtnLong){
                commandRepeatHandler.postDelayed(this, 1000);
            }else{
                commandRepeatHandler.postDelayed(this, 500);
            }

            flags.nextBtnLong = true;
            btnNext.startAnimation(animScale);
        }
    };

    Runnable repeatRewind = new Runnable() {
        @Override public void run() {

            btnPressCount.rewind++;
            sendQueuedCommand();

            if(!flags.prevBtnLong){
                commandRepeatHandler.postDelayed(this, 1000);
            }else{
                commandRepeatHandler.postDelayed(this, 500);
            }

            flags.prevBtnLong = true;
            btnPrev.startAnimation(animScale);
        }
    };

    private void setButtonBackgrounds(){

        int myColor = Color.parseColor(CommonData.btn_color);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(myColor));

        int btn_backgroundId_1, btn_backgroundId_2;

        switch (CommonData.btn_backgroundId){
            case 0:
                btn_backgroundId_1 = R.mipmap.bgnd1;
                btn_backgroundId_2 = R.mipmap.bgnd2;
                break;
            case 1:
                btn_backgroundId_1 = R.mipmap.bgnd3;
                btn_backgroundId_2 = R.mipmap.bgnd4;
                break;
            case 2:
                btn_backgroundId_1 = R.drawable.btn_background_3;
                btn_backgroundId_2 = btn_backgroundId_1;
                break;
            case 3:
                btn_backgroundId_1 = R.drawable.btn_background_4;
                btn_backgroundId_2 = btn_backgroundId_1;
                break;
            case 4:
                btn_backgroundId_1 = R.drawable.btn_background_5;
                btn_backgroundId_2 = btn_backgroundId_1;
                break;
            case 5:
                btn_backgroundId_1 = R.drawable.btn_background_6;
                btn_backgroundId_2 = btn_backgroundId_1;
                break;
            case 6:
                btn_backgroundId_1 = R.drawable.btn_background_7;
                btn_backgroundId_2 = btn_backgroundId_1;
                break;
            case 7:
                btn_backgroundId_1 = R.drawable.btn_background_8;
                btn_backgroundId_2 = btn_backgroundId_1;
                break;
            case 8:
                btn_backgroundId_1 = R.drawable.btn_background_9;
                btn_backgroundId_2 = btn_backgroundId_1;
                break;
            case 9:
                btn_backgroundId_1 = R.drawable.btn_background_10;
                btn_backgroundId_2 = btn_backgroundId_1;
                break;
            case 10:
                btn_backgroundId_1 = R.drawable.btn_background_11;
                btn_backgroundId_2 = btn_backgroundId_1;
                break;
            default:
                btn_backgroundId_1 = R.drawable.btn_background_12;
                btn_backgroundId_2 = btn_backgroundId_1;
                break;

        }

        btnShutDown.setBackgroundResource(btn_backgroundId_1);
        btnStop.setBackgroundResource(btn_backgroundId_1);
        btnMsg.setBackgroundResource(btn_backgroundId_2);
        btnMouse.setBackgroundResource(btn_backgroundId_2);

        if(CommonData.skinIdx == 1){

            if(CommonData.btn_backgroundId > 1){
                playBtnFrame.setBackgroundResource(R.drawable.btn_background_white);
                buttonGrid.setBackgroundResource(btn_backgroundId_1);

                btnPlay.setBackgroundResource(btn_backgroundId_1);
                btnPrev.setBackgroundResource(btn_backgroundId_1);
                btnNext.setBackgroundResource(btn_backgroundId_1);
                btnVolUp.setBackgroundResource(btn_backgroundId_1);
                btnVolDown.setBackgroundResource(btn_backgroundId_1);
            }else{
                playBtnFrame.setBackgroundColor(Color.TRANSPARENT);
                buttonGrid.setBackgroundResource(R.mipmap.background_2);

                btnPlay.setBackgroundColor(Color.TRANSPARENT);
                btnPrev.setBackgroundColor(Color.TRANSPARENT);
                btnNext.setBackgroundColor(Color.TRANSPARENT);
                btnVolUp.setBackgroundColor(Color.TRANSPARENT);
                btnVolDown.setBackgroundColor(Color.TRANSPARENT);
            }
        }else {
            btnPlay.setBackgroundResource(btn_backgroundId_1);
            btnPrev.setBackgroundResource(btn_backgroundId_1);
            btnNext.setBackgroundResource(btn_backgroundId_1);
            btnVolUp.setBackgroundResource(btn_backgroundId_1);
            btnVolDown.setBackgroundResource(btn_backgroundId_1);
        }

    }

    private void AskForDonation(){

        final String messageText = "It appears you have been using this app for a while." +
                " Please consider making a donation. No amount is too small. To donate go to:\n" +
                "http://radinaradionica.com/en/handicrafts/Programming";

        final SpannableString s = new SpannableString(messageText);
        Linkify.addLinks(s, Linkify.WEB_URLS);


        final AlertDialog AskForDonationDialog = new AlertDialog.Builder(Lazyfier.this)
                .setPositiveButton("Remind me\n" + "later", null)
                .setNegativeButton("Don't show\n" +
                        "again", new DialogInterface.OnClickListener() {
                    public void onClick (DialogInterface dialog,int which) {
                        preventDonateDialog();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage( s )
                .setTitle("Do you like the app?")
                .create();

        AskForDonationDialog.show();

        // Make the textview clickable. Must be called after show()
        ((TextView)AskForDonationDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void AskToRateOnPlayStore() {

        final String messageText = "Would you consider rating this app on Play Store? Your feedback would be appreciated.";

        final AlertDialog AskForDonationDialog = new AlertDialog.Builder(Lazyfier.this)
                .setPositiveButton("Rate App", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        preventRateDialog();

                        Uri uri = Uri.parse("market://details?id=" + getBaseContext().getPackageName());
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        // To count with Play market backstack, After pressing back button,
                        // to taken back to our application, we need to add following flags to intent.
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" + getBaseContext().getPackageName())));
                        }
                    }
                })
                .setNegativeButton("Don't show\n" + "again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        preventRateDialog();
                    }
                })
                .setMessage(messageText)
                .setTitle("Rate the app?")
                .create();

        AskForDonationDialog.show();
    }



    private void saveSkinSettings(){
        SharedPreferences prefs = getSharedPreferences("srv_prefs", CommonFunctions.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("skin_id", CommonData.skinIdx);
        editor.putInt("btn_backgroundId", CommonData.btn_backgroundId);
        editor.putString("btn_color", CommonData.btn_color);

        editor.apply();
    }

    private void saveUserPrefs(boolean doNotShowDonate, boolean doNotShowRate){
        SharedPreferences prefs = getSharedPreferences("srv_prefs", CommonFunctions.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if(doNotShowDonate){
            editor.putBoolean("show_donate_btn", false);
        }

        if(doNotShowRate){
            editor.putBoolean("show_rate_btn", false);
        }

        editor.apply();
    }

    private void preventDonateDialog(){
        saveUserPrefs(true, false);
    }

    private void preventRateDialog(){
        saveUserPrefs(false, true);
    }


}
