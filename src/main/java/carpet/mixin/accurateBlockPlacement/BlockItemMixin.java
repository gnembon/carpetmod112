package carpet.mixin.accurateBlockPlacement;

import carpet.CarpetSettings;
import carpet.helpers.BlockRotator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.PlaceableItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({
    BlockItem.class,
    PlaceableItem.class
})
public class BlockItemMixin {
    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getPlacementState(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;FFFILnet/minecraft/entity/LivingEntity;)Lnet/minecraft/block/BlockState;"))
    private BlockState getStateForPlacement(Block block, World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        if(CarpetSettings.accurateBlockPlacement && hitX > 1) {
            BlockState carpetState = BlockRotator.alternativeBlockPlacement(block, world, pos, facing, hitX, hitY, hitZ, meta, placer);
            if (carpetState != null) return carpetState;
        }
        return block.getPlacementState(world, pos, facing, hitX % 2.0F, hitY, hitZ, meta, placer);
    }
}
