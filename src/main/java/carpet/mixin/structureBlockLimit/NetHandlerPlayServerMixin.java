package carpet.mixin.structureBlockLimit;

import carpet.CarpetSettings;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.gen.structure.template.Template;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin {
    @Shadow @Final private MinecraftServer server;

    @ModifyConstant(method = "processCustomPayload", constant = {@Constant(intValue = -32), @Constant(intValue = 32)}, slice = @Slice(
        from = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityStructure;setName(Ljava/lang/String;)V"),
        to = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityStructure;setSize(Lnet/minecraft/util/math/BlockPos;)V")
    ))
    private int structureBlockLimit(int limit) {
        return limit < 0 ? -CarpetSettings.structureBlockLimit : CarpetSettings.structureBlockLimit;
    }

    // structure_block.load_prepare
    @Redirect(method = "processCustomPayload", at = @At(value = "NEW", target = "net/minecraft/util/text/TextComponentTranslation", ordinal = 10))
    private TextComponentTranslation errorMessage(String message, Object[] args) {
        String structureName = (String) args[0];
        Template template = server.worlds[0].getStructureTemplateManager().get(server, new ResourceLocation(structureName));
        if (template != null) {
            int sbl = CarpetSettings.structureBlockLimit;
            BlockPos size = template.getSize();
            if (size.getX() > sbl || size.getY() > sbl || size.getZ() > sbl) {
                return new TextComponentTranslation("Structure is too big for structure limit");
            }
        }
        return new TextComponentTranslation(message, args);
    }
}
