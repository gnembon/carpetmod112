package carpet.helpers;

import carpet.mixin.accessors.StatCraftingAccessor;
import net.minecraft.stats.StatCrafting;
import net.minecraft.util.text.ITextComponent;

public class StatSubItem extends StatCrafting {
    private final StatCrafting base;

    public StatSubItem(StatCrafting base, int meta, ITextComponent translation) {
        super(base.statId, "." + meta, translation, ((StatCraftingAccessor) base).getItem());
        this.base = base;
    }

    public StatCrafting getBase() {
        return this.base;
    }
}
