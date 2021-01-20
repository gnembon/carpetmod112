package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.class_2035;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(class_2035.class)
public class BlockDataCommandMixin {
    @Inject(method = "method_29272", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;fromTag(Lnet/minecraft/nbt/CompoundTag;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void recordBlockEdit(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci,
                                 BlockPos pos, World world, BlockState state, BlockEntity te, CompoundTag tag) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, pos, state, tag);
    }
}
