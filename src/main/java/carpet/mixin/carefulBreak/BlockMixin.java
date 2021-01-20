package carpet.mixin.carefulBreak;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.helpers.CarefulBreakHelper;
import carpet.logging.LoggerRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "dropStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setToDefaultPickupDelay()V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void doCarefulBreak(World world, BlockPos pos, ItemStack stack, CallbackInfo ci, float f, double d0, double d1, double d2, ItemEntity item) {
        ServerPlayerEntity player = CarefulBreakHelper.miningPlayer;
        if(CarpetSettings.carefulBreak && player != null && player.isSneaking() && LoggerRegistry.getLogger("carefulBreak").subscribed(player)){
            item.onPlayerCollision(player);
            if(item.removed){
                player.networkHandler.method_33624(new PlaySoundS2CPacket(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, pos.getX(), pos.getY(), pos.getZ(), 0.2F, (CarpetServer.rand.nextFloat() - CarpetServer.rand.nextFloat()) * 1.4F + 2.0F));
                ci.cancel();
            }
        }
    }
}
