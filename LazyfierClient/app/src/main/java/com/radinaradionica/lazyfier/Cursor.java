package com.radinaradionica.lazyfier;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class Cursor extends CommonFunctions implements SensorEventListener {

    /* Constants */
    static final int MAX_X = 65535;
    static final int MIN_SCROLL_DISTANCE = 50;
    static final float HISTORY_WEIGHT = 3f;
    static final int NEW_SAMPLE_COUNT = 10;
    static final int X_COORDINATE_MULTIPLIER = 700;
    static final int Y_COORDINATE_MULTIPLIER = 1000;
    static final int MIN_ZOOM_DISTANCE = 3;
    static final int MAX_ZOOM_FACTOR = 9;


    /* Objects */
    private android.hardware.SensorManager SensorManager;
    private Sensor sensor;
    private Button leftClick;
    private Button rightClick;

    /* Variables */
    private int xHistory, yHistory;
    private int xCurrent = (MAX_X + 1)/2;
    private int yCurrent = (MAX_X + 1)/2;
    private Boolean leftClickDown = false;
    private Boolean leftClickUp = false;
    private Boolean rightClickDown = false;
    private Boolean rightClickUp = false;
    private int scrollUp = 0;
    private int scrollDown = 0;
    private Boolean cursorPositionChanged = false;
    private Boolean txInProgress = false;
    private float scroll_y1;
    private float xAccumulator = 0;
    private float yAccumulator = 0;
    private int currentSampleCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cursor);

        setTitle("Lazyfier Mouse");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        initListeners();

        rightClick = (Button)findViewById(R.id.button_right);
        leftClick = (Button)findViewById(R.id.button_left);

        rightClick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.setAlpha(0.2f);
                        v.invalidate();

                        rightClickDown = true;
                        rightClickUp = true;
                        sendQueuedCommand();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.setAlpha(1);
                        v.invalidate();


                        sendQueuedCommand();
                        break;
                    }
                }
                return false;
            }
        });

        leftClick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.setAlpha(0.2f);
                        v.invalidate();

                        leftClickDown = true;
                        sendQueuedCommand();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.setAlpha(1);
                        v.invalidate();

                        leftClickUp = true;
                        sendQueuedCommand();
                        break;
                    }
                }
                return false;
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                scroll_y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float scroll_y2 = event.getY();
                float deltaY = scroll_y2 - scroll_y1;
                if (Math.abs(deltaY) > MIN_SCROLL_DISTANCE)
                {
                    if(scroll_y2 > scroll_y1){
                        scrollDown = Math.round((scroll_y2 - scroll_y1) / MIN_SCROLL_DISTANCE);
                    }else{
                        scrollUp = Math.round((scroll_y1 - scroll_y2) / MIN_SCROLL_DISTANCE);
                    }

                    sendQueuedCommand();
                }
//                else
//                {
//                    // consider as something else - a screen tap for example
//                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void initListeners()
    {
        SensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onResume()
    {
        initListeners();
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        SensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        xAccumulator += event.values[0];
        yAccumulator += event.values[1];
        currentSampleCount++;

        if(currentSampleCount == NEW_SAMPLE_COUNT){
            final float sampleDivider = NEW_SAMPLE_COUNT * (1 + HISTORY_WEIGHT);

            xAccumulator = xAccumulator / sampleDivider;
            yAccumulator = yAccumulator / sampleDivider;

            int xTemp = Math.round(xAccumulator * X_COORDINATE_MULTIPLIER);
            int yTemp = Math.round(yAccumulator * Y_COORDINATE_MULTIPLIER);

            int xDistance = xHistory - xTemp;
            int yDistance = yHistory - yTemp;
            xHistory = xTemp;
            yHistory = yTemp;

            int totalDistance = (int)Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));

            int zoom_factor = (totalDistance - MIN_ZOOM_DISTANCE) * 2 + 1;

            if(zoom_factor > MAX_ZOOM_FACTOR){
                zoom_factor = MAX_ZOOM_FACTOR;
            }else if(zoom_factor < 1){
                zoom_factor = 1;
            }

            xDistance *= zoom_factor;
            yDistance *= zoom_factor;

            xCurrent += xDistance;
            yCurrent += yDistance;

            if(xCurrent > MAX_X){
                xCurrent = MAX_X;
            }else if(xCurrent < 0){
                xCurrent = 0;
            }

            if(yCurrent > MAX_X){
                yCurrent = MAX_X;
            }else if(yCurrent < 0){
                yCurrent = 0;
            }

            xAccumulator *= NEW_SAMPLE_COUNT * HISTORY_WEIGHT;
            yAccumulator *= NEW_SAMPLE_COUNT * HISTORY_WEIGHT;

            currentSampleCount = 0;

            if(totalDistance == 0){
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }else{
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            cursorPositionChanged = true;
            sendQueuedCommand();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    private String sendMsgToHost(String payload){
        String msgToHost = "";

        if(common_ServerSetup.Password != ""){
            msgToHost = common_ServerSetup.Password + " ";
        }

        msgToHost += payload;

        String msgFromHost = common_queryHost(common_ServerSetup.Port, msgToHost, 80);
        if( !msgFromHost.contains("ACK")){
            if(msgFromHost == ""){
                msgFromHost = "No server response";
            }

            leftClickDown = false;
            leftClickUp = false;
            rightClickDown = false;
            rightClickUp = false;
            cursorPositionChanged = false;

            return("ERROR: " + msgFromHost + "!");
        }

        return("");
    }

    /* Sending commands to the server must be asynchronous so we do not block
    * the application in case of a longer timeout.
    * */
    private class AsyncSend extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            while(txInProgress) {

                String payload;

                if(scrollUp > 0) {
                    scrollUp--;
                    payload = "MOUSE_SCROLL_UP";
                }else if(scrollDown > 0) {
                    scrollDown--;
                    payload = "MOUSE_SCROLL_DOWN";
                }else if(leftClickDown){
                    leftClickDown = false;
                    payload = "MOUSE_LEFT_DOWN";
                }else if(leftClickUp){
                    leftClickUp = false;
                    payload = "MOUSE_LEFT_UP";
                }else if(rightClickDown){
                    rightClickDown = false;
                    payload = "MOUSE_RIGHT_DOWN";
                }else if(rightClickUp) {
                    rightClickUp = false;
                    payload = "MOUSE_RIGHT_UP";
                }else if(cursorPositionChanged){
                    cursorPositionChanged = false;
                    payload = "CURSOR:" + String.valueOf(xCurrent) + ":" + String.valueOf(yCurrent);
                }else{
                    txInProgress = false;
                    break;
                }

                String result = sendMsgToHost(payload);

                if(result != ""){
                    return result;
                }
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if((!leftClickDown) && (!leftClickUp) &&
                    (!rightClickDown) && (!rightClickUp) &&
                    (!cursorPositionChanged)){
                txInProgress = false;
            }
        }

        @Override
        protected void onPreExecute() {
            txInProgress = true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    /* Helper routine to start AsyncSend() which will keep going untill all commands are sent,
    * so there is no need to start it again untill it is finished
    * */
    private void sendQueuedCommand(){
        if(!txInProgress){
            AsyncSend sendMsg = new AsyncSend();
            sendMsg.execute();
        }
    }
}
