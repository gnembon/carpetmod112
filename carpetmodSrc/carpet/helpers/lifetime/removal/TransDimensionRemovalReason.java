package carpet.helpers.lifetime.removal;

import carpet.helpers.lifetime.utils.TextUtil;
import carpet.utils.Messenger;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;

import java.util.Objects;

public class TransDimensionRemovalReason extends RemovalReason
{
    private final DimensionType newDimension;

    public TransDimensionRemovalReason(DimensionType newDimension)
    {
        this.newDimension = Objects.requireNonNull(newDimension);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransDimensionRemovalReason that = (TransDimensionRemovalReason) o;
        return Objects.equals(this.newDimension, that.newDimension);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.newDimension);
    }

    @Override
    public ITextComponent toText()
    {
        ITextComponent dimText = TextUtil.getDimensionNameText(this.newDimension);
        dimText.getStyle().setColor(TextFormatting.GRAY);
        return Messenger.c("w Trans-dimension", "g  (to ", dimText, "g )");
    }
}
