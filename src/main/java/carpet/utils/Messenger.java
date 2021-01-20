package carpet.utils;

import carpet.CarpetSettings;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.class_2010;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Messenger
{

    /*
     messsage: "desc me ssa ge"
     desc contains:
     i = italic
     s = strikethrough
     u = underline
     b = bold
     o = obfuscated

     w = white
     y = yellow
     m = magenta (light purple)
     r = red
     c = cyan (aqua)
     l = lime (green)
     t = light blue (blue)
     f = dark gray
     g = gray
     d = gold
     p = dark purple (purple)
     n = dark red (brown)
     q = dark aqua
     e = dark green
     v = dark blue (navy)
     k = black

     / = action added to the previous component
     */

    private static Text _applyStyleToTextComponent(Text comp, String style)
    {
        //could be rewritten to be more efficient
        comp.getStyle().setItalic(style.indexOf('i')>=0);
        comp.getStyle().setStrikethrough(style.indexOf('s')>=0);
        comp.getStyle().setUnderline(style.indexOf('u')>=0);
        comp.getStyle().setBold(style.indexOf('b')>=0);
        comp.getStyle().setObfuscated(style.indexOf('o')>=0);
        comp.getStyle().setColor(Formatting.WHITE);
        if (style.indexOf('w')>=0) comp.getStyle().setColor(Formatting.WHITE); // not needed
        if (style.indexOf('y')>=0) comp.getStyle().setColor(Formatting.YELLOW);
        if (style.indexOf('m')>=0) comp.getStyle().setColor(Formatting.LIGHT_PURPLE);
        if (style.indexOf('r')>=0) comp.getStyle().setColor(Formatting.RED);
        if (style.indexOf('c')>=0) comp.getStyle().setColor(Formatting.AQUA);
        if (style.indexOf('l')>=0) comp.getStyle().setColor(Formatting.GREEN);
        if (style.indexOf('t')>=0) comp.getStyle().setColor(Formatting.BLUE);
        if (style.indexOf('f')>=0) comp.getStyle().setColor(Formatting.DARK_GRAY);
        if (style.indexOf('g')>=0) comp.getStyle().setColor(Formatting.GRAY);
        if (style.indexOf('d')>=0) comp.getStyle().setColor(Formatting.GOLD);
        if (style.indexOf('p')>=0) comp.getStyle().setColor(Formatting.DARK_PURPLE);
        if (style.indexOf('n')>=0) comp.getStyle().setColor(Formatting.DARK_RED);
        if (style.indexOf('q')>=0) comp.getStyle().setColor(Formatting.DARK_AQUA);
        if (style.indexOf('e')>=0) comp.getStyle().setColor(Formatting.DARK_GREEN);
        if (style.indexOf('v')>=0) comp.getStyle().setColor(Formatting.DARK_BLUE);
        if (style.indexOf('k')>=0) comp.getStyle().setColor(Formatting.BLACK);
        return comp;
    }
    public static String heatmap_color(double actual, double reference)
    {
        String color = "e";
        if (actual > 0.5D*reference) color = "y";
        if (actual > 0.8D*reference) color = "r";
        if (actual > reference) color = "m";
        return color;
    }
    public static String creatureTypeColor(EntityCategory type)
    {
        switch (type)
        {
            case MONSTER:
                return "n";
            case CREATURE:
                return "e";
            case AMBIENT:
                return "f";
            case WATER_CREATURE:
                return "v";
        }
        return "w";
    }

    private static Text _getChatComponentFromDesc(String message, Text previous_message)
    {
        String parts[] = message.split("\\s", 2);
        String desc = parts[0];
        String str = "";
        if (parts.length > 1) str = parts[1];
        if (desc.charAt(0) == '/') // deprecated
        {
            if (previous_message != null)
                previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message));
            return previous_message;
        }
        if (desc.charAt(0) == '?')
        {
            if (previous_message != null)
                previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message.substring(1)));
            return previous_message;
        }
        if (desc.charAt(0) == '!')
        {
            if (previous_message != null)
                previous_message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, message.substring(1)));
            return previous_message;
        }
        if (desc.charAt(0) == '^')
        {
            if (previous_message != null)
                previous_message.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Messenger.m(null, message.substring(1))));
            return previous_message;
        }
        Text txt = new LiteralText(str);
        return _applyStyleToTextComponent(txt, desc);
    }
    public static Text tp(String desc, BlockPos pos) { return tp(desc, pos.getX(), pos.getY(), pos.getZ()); }
    public static Text tp(String desc, double x, double y, double z) { return tp(desc, (float)x, (float)y, (float)z);}
    public static Text tp(String desc, float x, float y, float z)
    {
        return _getCoordsTextComponent(desc, x, y, z, false);
    }
    public static Text tp(String desc, int x, int y, int z)
    {
        return _getCoordsTextComponent(desc, (float)x, (float)y, (float)z, true);
    }

    public static Text tp(String desc, Waypoint waypoint) {
        String text = String.format("%s [ %.2f, %.2f, %.2f]", desc, waypoint.x, waypoint.y, waypoint.z);
        String command = "!/tp " + waypoint.getFullName();
        return m(null, text, command);
    }

    /// to be continued
    public static Text dbl(String style, double double_value)
    {
        return m(null, String.format("%s %.1f",style,double_value),String.format("^w %f",double_value));
    }
    public static Text dbls(String style, double ... doubles)
    {
        StringBuilder str = new StringBuilder(style + " [ ");
        String prefix = "";
        for (double dbl : doubles)
        {
            str.append(String.format("%s%.1f", prefix, dbl));
            prefix = ", ";
        }
        str.append(" ]");
        return m(null, str.toString());
    }
    public static Text dblf(String style, double ... doubles)
    {
        StringBuilder str = new StringBuilder(style + " [ ");
        String prefix = "";
        for (double dbl : doubles)
        {
            str.append(String.format("%s%f", prefix, dbl));
            prefix = ", ";
        }
        str.append(" ]");
        return m(null, str.toString());
    }
    public static Text dblt(String style, double ... doubles)
    {
        List<Object> components = new ArrayList<>();
        components.add(style+" [ ");
        String prefix = "";
        for (double dbl:doubles)
        {

            components.add(String.format("%s %s%.1f",style, prefix, dbl));
            components.add("?"+dbl);
            components.add("^w "+dbl);
            prefix = ", ";
        }
        //components.remove(components.size()-1);
        components.add(style+"  ]");
        return m(null, components.toArray(new Object[0]));
    }

    private static Text _getCoordsTextComponent(String style, float x, float y, float z, boolean isInt)
    {
        String text;
        String command;
        if (isInt)
        {
            text = String.format("%s [ %d, %d, %d ]",style, (int)x,(int)y, (int)z );
            command = String.format("!/tp %d %d %d",(int)x,(int)y, (int)z);
        }
        else
        {
            text = String.format("%s [ %.2f, %.2f, %.2f]",style, x, y, z);
            command = String.format("!/tp %f %f %f",x, y, z);
        }
        return m(null, text, command);
    }
    /*
    builds single line, multicomponent message, optionally returns it to sender, and returns as one chat messagge
     */
    public static Text m(class_2010 receiver, Object ... fields)
    {
        Text message = new LiteralText("");
        Text previous_component = null;
        for (Object o: fields)
        {
            if (o instanceof Text)
            {
                message.append((Text)o);
                previous_component = (Text)o;
                continue;
            }
            String txt = o.toString();
            //CarpetSettings.LOG.error(txt);
            Text comp = _getChatComponentFromDesc(txt,previous_component);
            if (comp != previous_component) message.append(comp);
            previous_component = comp;
        }
        if (receiver != null)
            receiver.sendMessage(message);
        return message;
    }

    public static Text s(class_2010 receiver,String text)
    {
        return s(receiver,text,"");
    }
    public static Text s(class_2010 receiver,String text, String style)
    {
        Text message = new LiteralText(text);
        _applyStyleToTextComponent(message, style);
        if (receiver != null)
            receiver.sendMessage(message);
        return message;
    }

    public static void send(class_2010 receiver, Text ... messages) { send(receiver, Arrays.asList(messages)); }
    public static void send(class_2010 receiver, List<Text> list)
    {
        list.forEach(receiver::sendMessage);
    }

    public static void print_server_message(MinecraftServer server, String message)
    {
        if (server == null)
            CarpetSettings.LOG.error("Message not delivered: "+message);
        server.sendMessage(new LiteralText(message));
        Text txt = m(null, "gi "+message);
        for (PlayerEntity entityplayer : server.getPlayerManager().getPlayerList())
        {
            entityplayer.sendMessage(txt);
        }
    }
    public static void print_server_message(MinecraftServer server, Text message)
    {
        if (server == null)
            CarpetSettings.LOG.error("Message not delivered: " + message.method_32275());
        server.sendMessage(message);
        for (PlayerEntity entityplayer : server.getPlayerManager().getPlayerList())
        {
            entityplayer.sendMessage(message);
        }
    }
}
