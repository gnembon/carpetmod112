package carpet.mixin.movableTileEntities;

import carpet.CarpetSettings;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Chunk.class)
public class ChunkMixin {
    @Shadow @Final private World world;

    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/Chunk$EnumCreateEntityType;)Lnet/minecraft/tileentity/TileEntity;"))
    private TileEntity betterGetTileEntity(Chunk chunk, BlockPos pos, Chunk.EnumCreateEntityType creationMode) {
        //this.getTileEntity(...) doesn't check pending TileEntities
        if (CarpetSettings.movableTileEntities || CarpetSettings.autocrafter) {
            return world.getTileEntity(pos);
        } else {
            return chunk.getTileEntity(pos, creationMode);
        }
    }
}
