package redstone.multimeter.util;

import java.awt.Color;

public class ColorUtils {
	
	private static int colorIndex = 0;
	
	public static int nextColor() {
		return nextColor(true);
	}
	
	public static int nextColor(boolean cycleIndex) {
		float hue = ((colorIndex * 11) % 8 + (colorIndex / 8) / 2.0F) / 8.0F;
		
		if (cycleIndex) {
			colorIndex = (colorIndex + 1) % 16;
		}
		
		return 0xFFFFFF & Color.HSBtoRGB(hue, 0.7F, 1.0F);
	}
	
	public static int getAlpha(int argb) {
		return (argb >> 24) & 0xFF;
	}
	
	public static int getRed(int argb) {
		return (argb >> 16) & 0xFF;
	}
	
	public static int getGreen(int argb) {
		return (argb >> 8) & 0xFF;
	}
	
	public static int getBlue(int argb) {
		return argb & 0xFF;
	}
	
	public static int fromAlpha(int alpha) {
		return (alpha & 0xFF) << 24;
	}
	
	public static int fromRed(int red) {
		return (red & 0xFF) << 16;
	}
	
	public static int fromGreen(int green) {
		return (green & 0xFF) << 8;
	}
	
	public static int fromBlue(int blue) {
		return blue & 0xFF;
	}
	
	public static int setAlpha(int color, int alpha) {
		return color & (~(0xFF << 24)) | (alpha << 24);
	}
	
	public static int setRed(int color, int red) {
		return color & (~(0xFF << 16)) | (red << 16);
	}
	
	public static int setGreen(int color, int green) {
		return color & (~(0xFF << 8)) | (green << 8);
	}
	
	public static int setBlue(int color, int blue) {
		return color & ~0xFF | blue;
	}
	
	public static int fromRGB(int red, int green, int blue) {
		return fromRed(red) | fromGreen(green) | fromBlue(blue);
	}
	
	public static int fromARGB(int alpha, int red, int green, int blue) {
		return fromARGB(alpha, fromRGB(red, green, blue));
	}
	
	public static int fromARGB(int alpha, int rgb) {
		return fromAlpha(alpha) | rgb;
	}
	
	public static int fromRGBString(String string) {
		if (string.length() > 6) {
			throw new NumberFormatException("Too many characters!");
		}
		
		return Integer.valueOf(string, 16);
	}
	
	public static int fromARGBString(String string) {
		if (string.length() > 8) {
			throw new NumberFormatException("Too many characters!");
		}
		
		return Integer.valueOf(string, 16);
	}
	
	public static String toRGBString(int color) {
		String hex = Integer.toHexString(color & 0xFFFFFF);
		
		while (hex.length() < 6) {
			hex = "0" + hex;
		}
		
		return hex.toUpperCase();
	}
	
	public static String toARGBString(int color) {
		String hex = Integer.toHexString(color & 0xFFFFFFFF);
		
		while (hex.length() < 8) {
			hex = "0" + hex;
		}
		
		return hex.toUpperCase();
	}
}
