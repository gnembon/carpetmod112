package carpet.mixin.skyblock;

import carpet.CarpetSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Mixin(Chunk.class)
public class ChunkMixin {
    @Shadow @Final private ClassInheritanceMultiMap<Entity>[] entityLists;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;
    @Shadow private int heightMapMinimum;
    @Shadow @Final private World world;
    @Shadow @Final private int[] heightMap;
    @Shadow @Final private ExtendedBlockStorage[] storageArrays;

    @Shadow @Final public int x;

    @Shadow @Final public int z;

    @Inject(method = "populate(Lnet/minecraft/world/gen/IChunkGenerator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/IChunkGenerator;populate(II)V", shift = At.Shift.AFTER))
    private void afterPopulate(IChunkGenerator generator, CallbackInfo ci) {
        // Skyblock in carpet 12
        if(CarpetSettings.skyblock) {
            for(int i = 0; i < 4; i++) {
                if(world.isChunkGeneratedAt(x + i % 2, z + i / 2)) {
                    ((ChunkMixin) (Object) world.getChunk(x + i % 2, z + i / 2)).removeAllBlocks();
                }
            }
        }
    }

    public void removeAllBlocks() {
        IBlockState air = Blocks.AIR.getDefaultState();
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 256; ++l) {
                    int i1 = l >> 4;
                    try {
                        if (this.storageArrays[i1] != null && this.storageArrays[i1].get(j, l & 15, k).getBlock() != Blocks.END_PORTAL_FRAME) {
                            this.storageArrays[i1].set(j, l & 15, k, air);
                            if (this.storageArrays[i1].getSkyLight() != null) {
                                this.storageArrays[i1].setSkyLight(j, l & 15, k, 15);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        if (tileEntities != null) {
            tileEntities.clear();
        }
        if (entityLists != null) {
            Set<Entity> list = new LinkedHashSet<>();
            for (ClassInheritanceMultiMap<Entity> entityList : entityLists) {
                list.addAll(entityList);
            }
            for (Entity e : list) {
                e.setDead();
            }
        }
        heightMapMinimum = 0;
        if (world.provider.hasSkyLight()) {
            Arrays.fill(heightMap, 15);
        }
    }
}
