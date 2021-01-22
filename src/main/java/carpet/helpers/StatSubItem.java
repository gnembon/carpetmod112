package carpet.helpers;

import carpet.mixin.accessors.StatCraftingAccessor;
import net.minecraft.stat.ItemStat;
import net.minecraft.text.Text;

public class StatSubItem extends ItemStat {
    private final ItemStat base;

    public StatSubItem(ItemStat base, int meta, Text translation) {
        super(base.field_32604, "." + meta, translation, ((StatCraftingAccessor) base).getItem());
        this.base = base;
    }

    public ItemStat getBase() {
        return this.base;
    }
}
