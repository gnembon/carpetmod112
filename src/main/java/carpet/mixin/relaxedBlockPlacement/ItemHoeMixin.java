package carpet.mixin.relaxedBlockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemHoe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemHoe.class)
public class ItemHoeMixin {
    @Redirect(method = "onItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getMaterial()Lnet/minecraft/block/material/Material;"))
    private Material allowWater(IBlockState state) {
        Material material = state.getMaterial();
        if (CarpetSettings.relaxedBlockPlacement && material == Material.WATER) return Material.AIR;
        return material;
    }
}
