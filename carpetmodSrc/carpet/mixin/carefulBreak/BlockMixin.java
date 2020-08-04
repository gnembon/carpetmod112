package carpet.mixin.carefulBreak;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.logging.LoggerRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "spawnAsEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;setDefaultPickupDelay()V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void doCarefulBreak(World worldIn, BlockPos pos, ItemStack stack, CallbackInfo ci, float f, double d0, double d1, double d2, EntityItem item) {
        if(CarpetSettings.carefulBreak && PlayerInteractionManager.playerMinedBlock != null && PlayerInteractionManager.playerMinedBlock.isSneaking() && LoggerRegistry.getLogger("carefulBreak").subscribed(PlayerInteractionManager.playerMinedBlock)){
            item.onCollideWithPlayer(PlayerInteractionManager.playerMinedBlock);
            if(item.isDead){
                PlayerInteractionManager.playerMinedBlock.connection.sendPacket(new SPacketSoundEffect(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, pos.getX(), pos.getY(), pos.getZ(), 0.2F, (CarpetServer.rand.nextFloat() - CarpetServer.rand.nextFloat()) * 1.4F + 2.0F));
                ci.cancel();
            }
        }
    }
}
