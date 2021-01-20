package carpet.mixin.carefulBreak;

import carpet.helpers.CarefulBreakHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow public ServerPlayerEntity player;

    @Redirect(method = "method_33540", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;afterBreak(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/item/ItemStack;)V"))
    private void harvestBlock(Block block, World worldIn, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity te, ItemStack stack) {
        try {
            CarefulBreakHelper.miningPlayer = this.player;
            block.afterBreak(worldIn, player, pos, state, te, stack);
        } finally {
            CarefulBreakHelper.miningPlayer = null;
        }
    }
}
