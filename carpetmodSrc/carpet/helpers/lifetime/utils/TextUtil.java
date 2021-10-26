package carpet.helpers.lifetime.utils;

import carpet.utils.Messenger;
import com.google.common.collect.Maps;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.DimensionType;

import java.util.Map;

public class TextUtil
{
    public static ITextComponent attachHoverEvent(ITextComponent text, HoverEvent hoverEvent)
    {
        text.getStyle().setHoverEvent(hoverEvent);
        return text;
    }

    public static ITextComponent attachHoverText(ITextComponent text, ITextComponent hoverText)
    {
        return attachHoverEvent(text, new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
    }

    private static final Map<DimensionType, ITextComponent> DIMENSION_NAME = Maps.newHashMap();

    static
    {
        DIMENSION_NAME.put(DimensionType.OVERWORLD, new TextComponentTranslation("createWorld.customize.preset.overworld"));
        DIMENSION_NAME.put(DimensionType.NETHER, new TextComponentTranslation("advancements.nether.root.title"));
        DIMENSION_NAME.put(DimensionType.THE_END, new TextComponentTranslation("advancements.end.root.title"));
    }

    public static String getTeleportCommand(Vec3d pos, DimensionType dimensionType)
    {
        // no "execute in <dimension>" in 1.12
        return String.format("/tp %s %s %s", pos.x, pos.y, pos.z);
    }

    public static ITextComponent getFancyText(String style, ITextComponent displayText, ITextComponent hoverText, ClickEvent clickEvent)
    {
        ITextComponent text = displayText.createCopy();
        if (style != null)
        {
            text.setStyle(Messenger.c(style + "  ").getSiblings().get(0).getStyle());
        }
        if (hoverText != null)
        {
            text.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        }
        if (clickEvent != null)
        {
            text.getStyle().setClickEvent(clickEvent);
        }
        return text;
    }

    public static String getCoordinateString(Vec3d pos)
    {
        return String.format("[%.1f, %.1f, %.1f]", pos.x, pos.y, pos.z);
    }

    public static ITextComponent getDimensionNameText(DimensionType dim)
    {
        return DIMENSION_NAME.getOrDefault(dim, Messenger.s(null, dim.toString())).createCopy();
    }
}
