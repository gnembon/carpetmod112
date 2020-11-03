package carpet.utils;

import carpet.CarpetSettings;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

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

    private static ITextComponent _applyStyleToTextComponent(ITextComponent comp, String style)
    {
        //could be rewritten to be more efficient
        comp.getStyle().setItalic(style.indexOf('i')>=0);
        comp.getStyle().setStrikethrough(style.indexOf('s')>=0);
        comp.getStyle().setUnderlined(style.indexOf('u')>=0);
        comp.getStyle().setBold(style.indexOf('b')>=0);
        comp.getStyle().setObfuscated(style.indexOf('o')>=0);
        comp.getStyle().setColor(TextFormatting.WHITE);
        if (style.indexOf('w')>=0) comp.getStyle().setColor(TextFormatting.WHITE); // not needed
        if (style.indexOf('y')>=0) comp.getStyle().setColor(TextFormatting.YELLOW);
        if (style.indexOf('m')>=0) comp.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
        if (style.indexOf('r')>=0) comp.getStyle().setColor(TextFormatting.RED);
        if (style.indexOf('c')>=0) comp.getStyle().setColor(TextFormatting.AQUA);
        if (style.indexOf('l')>=0) comp.getStyle().setColor(TextFormatting.GREEN);
        if (style.indexOf('t')>=0) comp.getStyle().setColor(TextFormatting.BLUE);
        if (style.indexOf('f')>=0) comp.getStyle().setColor(TextFormatting.DARK_GRAY);
        if (style.indexOf('g')>=0) comp.getStyle().setColor(TextFormatting.GRAY);
        if (style.indexOf('d')>=0) comp.getStyle().setColor(TextFormatting.GOLD);
        if (style.indexOf('p')>=0) comp.getStyle().setColor(TextFormatting.DARK_PURPLE);
        if (style.indexOf('n')>=0) comp.getStyle().setColor(TextFormatting.DARK_RED);
        if (style.indexOf('q')>=0) comp.getStyle().setColor(TextFormatting.DARK_AQUA);
        if (style.indexOf('e')>=0) comp.getStyle().setColor(TextFormatting.DARK_GREEN);
        if (style.indexOf('v')>=0) comp.getStyle().setColor(TextFormatting.DARK_BLUE);
        if (style.indexOf('k')>=0) comp.getStyle().setColor(TextFormatting.BLACK);
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
    public static String creatureTypeColor(EnumCreatureType type)
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

    private static ITextComponent _getChatComponentFromDesc(String message, ITextComponent previous_message)
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
        ITextComponent txt = new TextComponentString(str);
        return _applyStyleToTextComponent(txt, desc);
    }
    public static ITextComponent tp(String desc, BlockPos pos) { return tp(desc, pos.getX(), pos.getY(), pos.getZ()); }
    public static ITextComponent tp(String desc, double x, double y, double z) { return tp(desc, (float)x, (float)y, (float)z);}
    public static ITextComponent tp(String desc, float x, float y, float z)
    {
        return _getCoordsTextComponent(desc, x, y, z, false);
    }
    public static ITextComponent tp(String desc, int x, int y, int z)
    {
        return _getCoordsTextComponent(desc, (float)x, (float)y, (float)z, true);
    }

    public static ITextComponent tp(String desc, Waypoint waypoint) {
        String text = String.format("%s [ %.2f, %.2f, %.2f]", desc, waypoint.x, waypoint.y, waypoint.z);
        String command = "!/tp " + waypoint.getFullName();
        return m(null, text, command);
    }

    /// to be continued
    public static ITextComponent dbl(String style, double double_value)
    {
        return m(null, String.format("%s %.1f",style,double_value),String.format("^w %f",double_value));
    }
    public static ITextComponent dbls(String style, double ... doubles)
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
    public static ITextComponent dblf(String style, double ... doubles)
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
    public static ITextComponent dblt(String style, double ... doubles)
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

    private static ITextComponent _getCoordsTextComponent(String style, float x, float y, float z, boolean isInt)
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
    public static ITextComponent m(ICommandSender receiver, Object ... fields)
    {
        ITextComponent message = new TextComponentString("");
        ITextComponent previous_component = null;
        for (Object o: fields)
        {
            if (o instanceof ITextComponent)
            {
                message.appendSibling((ITextComponent)o);
                previous_component = (ITextComponent)o;
                continue;
            }
            String txt = o.toString();
            //CarpetSettings.LOG.error(txt);
            ITextComponent comp = _getChatComponentFromDesc(txt,previous_component);
            if (comp != previous_component) message.appendSibling(comp);
            previous_component = comp;
        }
        if (receiver != null)
            receiver.sendMessage(message);
        return message;
    }

    public static ITextComponent s(ICommandSender receiver,String text)
    {
        return s(receiver,text,"");
    }
    public static ITextComponent s(ICommandSender receiver,String text, String style)
    {
        ITextComponent message = new TextComponentString(text);
        _applyStyleToTextComponent(message, style);
        if (receiver != null)
            receiver.sendMessage(message);
        return message;
    }

    public static void send(ICommandSender receiver, ITextComponent ... messages) { send(receiver, Arrays.asList(messages)); }
    public static void send(ICommandSender receiver, List<ITextComponent> list)
    {
        list.forEach(receiver::sendMessage);
    }

    public static void print_server_message(MinecraftServer server, String message)
    {
        if (server == null)
            CarpetSettings.LOG.error("Message not delivered: "+message);
        server.sendMessage(new TextComponentString(message));
        ITextComponent txt = m(null, "gi "+message);
        for (EntityPlayer entityplayer : server.getPlayerList().getPlayers())
        {
            entityplayer.sendMessage(txt);
        }
    }
    public static void print_server_message(MinecraftServer server, ITextComponent message)
    {
        if (server == null)
            CarpetSettings.LOG.error("Message not delivered: "+message.getUnformattedText());
        server.sendMessage(message);
        for (EntityPlayer entityplayer : server.getPlayerList().getPlayers())
        {
            entityplayer.sendMessage(message);
        }
    }
}
