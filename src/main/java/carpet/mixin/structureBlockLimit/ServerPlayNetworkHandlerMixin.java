package carpet.mixin.structureBlockLimit;

import carpet.CarpetSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.structure.Structure;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow @Final private MinecraftServer server;

    @ModifyConstant(method = "onCustomPayload", constant = {@Constant(intValue = -32), @Constant(intValue = 32)}, slice = @Slice(
        from = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/StructureBlockBlockEntity;setName(Ljava/lang/String;)V"),
        to = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/StructureBlockBlockEntity;setSize(Lnet/minecraft/util/math/BlockPos;)V")
    ))
    private int structureBlockLimit(int limit) {
        return limit < 0 ? -CarpetSettings.structureBlockLimit : CarpetSettings.structureBlockLimit;
    }

    // structure_block.load_prepare
    @Redirect(method = "onCustomPayload", at = @At(value = "NEW", target = "net/minecraft/text/TranslatableText", ordinal = 10))
    private TranslatableText errorMessage(String message, Object[] args) {
        String structureName = (String) args[0];
        Structure template = server.worlds[0].getStructureManager().method_27994(server, new Identifier(structureName));
        if (template != null) {
            int sbl = CarpetSettings.structureBlockLimit;
            BlockPos size = template.getSize();
            if (size.getX() > sbl || size.getY() > sbl || size.getZ() > sbl) {
                return new TranslatableText("Structure is too big for structure limit");
            }
        }
        return new TranslatableText(message, args);
    }
}
