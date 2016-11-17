/**
 * Created by Rada Berar
 */
package com.radinaradionica.lazyfier;

import java.net.DatagramSocket;


public class CommonData {
    public static final int ValidPorts[] = {2000, 3000, 4000, 5000};
    public static final int BROADCAST_RCV_PORT = 3014;

    public static int skinIdx;      /* Id of the currently used skin */
    public static int newSkinIdx;   /* Id of the requested skin */
    public static int btn_backgroundId;
    public static String btn_color;

    public static Boolean skinSettingsChanged;

    public static DatagramSocket ReceiveSocket;

    public static int startupCount;
    public static boolean showDonate;
    public static boolean showRate;

}
