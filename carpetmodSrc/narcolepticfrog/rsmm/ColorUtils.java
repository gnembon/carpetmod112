package narcolepticfrog.rsmm;

import java.awt.*;
import java.util.Random;

public class ColorUtils {

    private static Random rand = new Random();

    private static int colorIndex = 0;

    /**
     * Generates a random color, excluding colors that are too dark.
     */
    public static int randomColor() {
        return hsb2int(rand.nextFloat(),  rand.nextFloat()*0.3F+0.7F, rand.nextFloat()*0.3F + 0.7F);
    }

    public static int hsb2int(float h, float s, float b) {
        Color c = Color.getHSBColor(h,s,b);
        int color = 0xFF000000;
        color |= c.getBlue();
        color |= c.getGreen() << 8;
        color |= c.getRed() << 16;
        return color;
    }

    public static int nextColor() {
        float hue = ((colorIndex*11)%8 + (colorIndex/8)/2.0f) / 8f;
        int color = hsb2int(hue, 0.7f, 1.0f);
        colorIndex = (colorIndex + 1) % 16;
        return color;
    }

    public static int parseColor(String str) {
        if (str.length() < 6) {
            return 0;
        }
        try {
            int r = Integer.valueOf(str.substring(str.length() - 6, str.length() - 4), 16);
            int g = Integer.valueOf(str.substring(str.length() - 4, str.length() - 2), 16);
            int b = Integer.valueOf(str.substring(str.length() - 2), 16);
            return (0xFF000000) | (r << 16) | (g << 8) | b;
        } catch (Exception e) {
            return 0;
        }
    }

}
