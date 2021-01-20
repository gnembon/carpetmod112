package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Redirect(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;interactBlock(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;FFF)Lnet/minecraft/util/ActionResult;"))
    private ActionResult processRightClickBlock(ServerPlayerInteractionManager manager, PlayerEntity player, World worldIn, ItemStack stack, Hand hand, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ) {
        try {
            CarpetClientChunkLogger.setReason("Player interacting with right click");
            return manager.interactBlock(player, worldIn, stack, hand, pos, facing, hitX, hitY, hitZ);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
