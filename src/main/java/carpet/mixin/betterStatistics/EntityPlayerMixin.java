package carpet.mixin.betterStatistics;

import carpet.helpers.StatHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayer.class)
public class EntityPlayerMixin {
    @Redirect(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/StatList;getDroppedObjectStats(Lnet/minecraft/item/Item;)Lnet/minecraft/stats/StatBase;"))
    private StatBase addDroppedObjectMeta(Item item, ItemStack stack) {
        return StatHelper.getDroppedObjectStats(item, stack.getItemDamage());
    }
}
