package redstone.multimeter.util;

import java.util.List;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TextUtils {
	
	public static void addFancyText(List<ITextComponent> lines, String title, Object info) {
		addFancyText(lines, title, info.toString());
	}
	
	public static void addFancyText(List<ITextComponent> lines, String title, String info) {
		lines.add(formatFancyText(title, info));
	}
	
	public static ITextComponent formatFancyText(String title, Object info) {
		return new TextComponentString("").
			appendSibling(new TextComponentString(title + ": ").setStyle(new Style().setColor(TextFormatting.GOLD))).
			appendSibling(new TextComponentString(info.toString()));
	}
}
