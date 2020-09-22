package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.item.Item;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StatList.class)
public class StatListMixin {
    @Redirect(method = "initCraftableStats", at = @At(value = "NEW", target = "net/minecraft/stats/StatCrafting"))
    private static StatCrafting createCraftStat(String id1, String id2, ITextComponent text, Item item) {
        StatCrafting baseStat = new StatCrafting(id1, id2, text, item);
        StatHelper.addCraftStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "initMiningStats", at = @At(value = "NEW", target = "net/minecraft/stats/StatCrafting"))
    private static StatCrafting createMiningStat(String id1, String id2, ITextComponent text, Item item) {
        StatCrafting baseStat = new StatCrafting(id1, id2, text, item);
        StatHelper.addMineStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "initStats", at = @At(value = "NEW", target = "net/minecraft/stats/StatCrafting"))
    private static StatCrafting createUseStat(String id1, String id2, ITextComponent text, Item item) {
        StatCrafting baseStat = new StatCrafting(id1, id2, text, item);
        StatHelper.addUseStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "initPickedUpAndDroppedStats", at = @At(value = "NEW", target = "net/minecraft/stats/StatCrafting", ordinal = 0))
    private static StatCrafting createPickedUpStat(String id1, String id2, ITextComponent text, Item item) {
        StatCrafting baseStat = new StatCrafting(id1, id2, text, item);
        StatHelper.addPickedUpStats(baseStat);
        return baseStat;
    }

    @Redirect(method = "initPickedUpAndDroppedStats", at = @At(value = "NEW", target = "net/minecraft/stats/StatCrafting", ordinal = 1))
    private static StatCrafting createDroppedStat(String id1, String id2, ITextComponent text, Item item) {
        StatCrafting baseStat = new StatCrafting(id1, id2, text, item);
        StatHelper.addDroppedStats(baseStat);
        return baseStat;
    }
}
