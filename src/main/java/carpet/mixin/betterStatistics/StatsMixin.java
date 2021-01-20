package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.item.Item;
import net.minecraft.class_2590;
import net.minecraft.stats.Stats;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Stats.class)
public class StatsMixin {
    @Redirect(method = "method_33891", at = @At(value = "NEW", target = "net/minecraft/class_2590"))
    private static class_2590 createCraftStat(String id1, String id2, Text text, Item item) {
        class_2590 baseStat = new class_2590(id1, id2, text, item);
        StatHelper.addCraftStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "method_33894", at = @At(value = "NEW", target = "net/minecraft/class_2590"))
    private static class_2590 createMiningStat(String id1, String id2, Text text, Item item) {
        class_2590 baseStat = new class_2590(id1, id2, text, item);
        StatHelper.addMineStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "method_33896", at = @At(value = "NEW", target = "net/minecraft/class_2590"))
    private static class_2590 createUseStat(String id1, String id2, Text text, Item item) {
        class_2590 baseStat = new class_2590(id1, id2, text, item);
        StatHelper.addUseStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "method_33900", at = @At(value = "NEW", target = "net/minecraft/class_2590", ordinal = 0))
    private static class_2590 createPickedUpStat(String id1, String id2, Text text, Item item) {
        class_2590 baseStat = new class_2590(id1, id2, text, item);
        StatHelper.addPickedUpStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "method_33900", at = @At(value = "NEW", target = "net/minecraft/class_2590", ordinal = 1))
    private static class_2590 createDroppedStat(String id1, String id2, Text text, Item item) {
        class_2590 baseStat = new class_2590(id1, id2, text, item);
        StatHelper.addDroppedStats(baseStat);
        return baseStat;
    }
}
