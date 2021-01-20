package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Redirect(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/Stats;method_33899(Lnet/minecraft/item/Item;)Lnet/minecraft/stat/Stat;"))
    private Stat addDroppedObjectMeta(Item item, ItemStack stack) {
        return StatHelper.getDroppedObjectStats(item, stack.getDamage());
    }
}
