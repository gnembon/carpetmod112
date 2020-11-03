package carpet.mixin.flippinCactus;

import carpet.helpers.BlockRotator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerInteractionManager.class)
public class PlayerInteractionManagerMixin {
    @Shadow public EntityPlayerMP player;

    @Inject(method = "processRightClickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void tryFlipWithCactus(EntityPlayer player, World world, ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<EnumActionResult> cir, IBlockState blockState) {
        //flip method will check for flippinCactus setting
        if (BlockRotator.flipBlockWithCactus(world, pos, blockState, player, hand, facing, hitX, hitY, hitZ)) {
            cir.setReturnValue(EnumActionResult.PASS);
        }
    }
}
