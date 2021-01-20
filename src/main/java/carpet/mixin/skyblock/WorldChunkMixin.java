package carpet.mixin.skyblock;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilterableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
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

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
    @Shadow @Final private TypeFilterableList<Entity>[] entitySections;
    @Shadow @Final private Map<BlockPos, BlockEntity> blockEntities;
    @Shadow private int field_25385;
    @Shadow @Final private World world;
    @Shadow @Final private int[] field_25375;
    @Shadow @Final private ChunkSection[] sections;
    @Shadow @Final public int field_25365;
    @Shadow @Final public int field_25366;

    @Inject(method = "method_27367", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkManager;method_27344(II)V", shift = At.Shift.AFTER))
    private void afterPopulate(ChunkManager generator, CallbackInfo ci) {
        // Skyblock in carpet 12
        if(CarpetSettings.skyblock) {
            for(int i = 0; i < 4; i++) {
                if(world.method_26067(field_25365 + i % 2, field_25366 + i / 2)) {
                    ((WorldChunkMixin) (Object) world.method_25975(field_25365 + i % 2, field_25366 + i / 2)).removeAllBlocks();
                }
            }
        }
    }

    public void removeAllBlocks() {
        BlockState air = Blocks.AIR.getDefaultState();
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 256; ++l) {
                    int i1 = l >> 4;
                    try {
                        if (this.sections[i1] != null && this.sections[i1].method_27435(j, l & 15, k).getBlock() != Blocks.END_PORTAL_FRAME) {
                            this.sections[i1].method_27437(j, l & 15, k, air);
                            if (this.sections[i1].method_27448() != null) {
                                this.sections[i1].method_27436(j, l & 15, k, 15);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        if (blockEntities != null) {
            blockEntities.clear();
        }
        if (entitySections != null) {
            Set<Entity> list = new LinkedHashSet<>();
            for (TypeFilterableList<Entity> entityList : entitySections) {
                list.addAll(entityList);
            }
            for (Entity e : list) {
                e.remove();
            }
        }
        field_25385 = 0;
        if (world.dimension.hasSkyLight()) {
            Arrays.fill(field_25375, 15);
        }
    }
}
