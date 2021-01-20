package carpet.mixin.flippinCactus;

import carpet.helpers.BlockRotator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void tryFlipWithCactus(PlayerEntity player, World world, ItemStack stack, Hand hand, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<ActionResult> cir, BlockState blockState) {
        //flip method will check for flippinCactus setting
        if (BlockRotator.flipBlockWithCactus(world, pos, blockState, player, hand, facing, hitX, hitY, hitZ)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
