package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.item.Item;
import net.minecraft.stat.ItemStat;
import net.minecraft.stats.Stats;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Stats.class)
public class StatsMixin {
    @Redirect(method = "method_33891", at = @At(value = "NEW", target = "net/minecraft/stat/ItemStat"))
    private static ItemStat createCraftStat(String id1, String id2, Text text, Item item) {
        ItemStat baseStat = new ItemStat(id1, id2, text, item);
        StatHelper.addCraftStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "method_33894", at = @At(value = "NEW", target = "net/minecraft/stat/ItemStat"))
    private static ItemStat createMiningStat(String id1, String id2, Text text, Item item) {
        ItemStat baseStat = new ItemStat(id1, id2, text, item);
        StatHelper.addMineStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "method_33896", at = @At(value = "NEW", target = "net/minecraft/stat/ItemStat"))
    private static ItemStat createUseStat(String id1, String id2, Text text, Item item) {
        ItemStat baseStat = new ItemStat(id1, id2, text, item);
        StatHelper.addUseStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "method_33900", at = @At(value = "NEW", target = "net/minecraft/stat/ItemStat", ordinal = 0))
    private static ItemStat createPickedUpStat(String id1, String id2, Text text, Item item) {
        ItemStat baseStat = new ItemStat(id1, id2, text, item);
        StatHelper.addPickedUpStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "method_33900", at = @At(value = "NEW", target = "net/minecraft/stat/ItemStat", ordinal = 1))
    private static ItemStat createDroppedStat(String id1, String id2, Text text, Item item) {
        ItemStat baseStat = new ItemStat(id1, id2, text, item);
        StatHelper.addDroppedStats(baseStat);
        return baseStat;
    }
}
