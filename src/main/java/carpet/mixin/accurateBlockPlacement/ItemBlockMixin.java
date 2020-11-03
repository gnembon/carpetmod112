package carpet.mixin.accurateBlockPlacement;

import carpet.CarpetSettings;
import carpet.helpers.BlockRotator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({
    ItemBlock.class,
    ItemBlockSpecial.class
})
public class ItemBlockMixin {
    @Redirect(method = "onItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getStateForPlacement(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFFILnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState getStateForPlacement(Block block, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if(CarpetSettings.accurateBlockPlacement && hitX > 1) {
            IBlockState carpetState = BlockRotator.alternativeBlockPlacement(block, world, pos, facing, hitX, hitY, hitZ, meta, placer);
            if (carpetState != null) return carpetState;
        }
        return block.getStateForPlacement(world, pos, facing, hitX % 2.0F, hitY, hitZ, meta, placer);
    }
}
