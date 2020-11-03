package carpet.mixin.accessors;

import net.minecraft.scoreboard.ScoreCriteriaStat;
import net.minecraft.stats.StatBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScoreCriteriaStat.class)
public interface ScoreCriteriaStatAccessor {
    @Accessor StatBase getStat();
}
