package carpet.helpers.lifetime.spawning;

import carpet.helpers.lifetime.utils.TextUtil;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;

import java.util.Objects;

public class TransDimensionSpawningReason extends SpawningReason
{
    private final DimensionType oldDimension;

    public TransDimensionSpawningReason(DimensionType oldDimension)
    {
        this.oldDimension = Objects.requireNonNull(oldDimension);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransDimensionSpawningReason that = (TransDimensionSpawningReason) o;
        return Objects.equals(this.oldDimension, that.oldDimension);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.oldDimension);
    }

    @Override
    public ITextComponent toText()
    {
        ITextComponent dimText = TextUtil.getDimensionNameText(this.oldDimension);
        dimText.getStyle().setColor(TextFormatting.GRAY);
        return Messenger.c("w Trans-dimension", "g  (from ", dimText, "g )");
    }
}
