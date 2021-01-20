package carpet.helpers;

import carpet.mixin.accessors.StatCraftingAccessor;
import net.minecraft.class_2590;
import net.minecraft.text.Text;

public class StatSubItem extends class_2590 {
    private final class_2590 base;

    public StatSubItem(class_2590 base, int meta, Text translation) {
        super(base.field_32604, "." + meta, translation, ((StatCraftingAccessor) base).getItem());
        this.base = base;
    }

    public class_2590 getBase() {
        return this.base;
    }
}
