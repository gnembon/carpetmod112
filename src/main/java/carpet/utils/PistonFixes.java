package carpet.utils;

import carpet.carpetclient.CarpetClientMessageHandler;

public class PistonFixes {

    private static boolean pistonFix = false;

    public static void synchronizeClient() {
        if(pistonFix) {
            CarpetClientMessageHandler.sendPistonUpdate();
            pistonFix = false;
        }
    }

    public static void onEndTick() {
        pistonFix = true;
    }

}
