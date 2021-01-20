package carpet.mixin.newLight;

import carpet.CarpetSettings;
import carpet.helpers.LightingHooks;
import carpet.utils.extensions.NewLightChunk;
import carpet.utils.extensions.NewLightWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements NewLightChunk {
    private short[] neighborLightChecks = null;
    private short pendingNeighborLightInits;

    @Shadow @Final private World world;
    @Shadow @Final private ChunkSection[] sections;
    @Shadow private boolean field_25379;
    @Shadow private boolean field_25380;
    @Shadow @Final public int field_25365;
    @Shadow @Final public int field_25366;
    @Shadow public abstract boolean method_27396(BlockPos pos);
    @Shadow public abstract void method_27421();
    @Shadow protected abstract void method_27399(int x, int z);

    @Inject(method = "method_27385", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;method_27521()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void generateSkyLight(CallbackInfo ci, int top, int x, int z) {
        if (CarpetSettings.newLight) LightingHooks.fillSkylightColumn((WorldChunk) (Object) this, x, z);
    }

    @Redirect(method = "method_27385", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;method_27521()Z"))
    private boolean generateSkyLightCancelVanilla(Dimension worldProvider) {
        return !CarpetSettings.newLight && worldProvider.method_27521();
    }

    @ModifyConstant(method = "method_27394", constant = @Constant(intValue = 255))
    private int noMask(int mask) {
        return CarpetSettings.newLight ? -1 : 255;
    }

    @Redirect(method = "method_27394", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_25977(IIII)V"))
    private void markDirtyInRelightBlock(World world, int x, int z, int y1, int y2) {
        if (!CarpetSettings.newLight) world.method_25977(x, z, y1, y2);
    }

    @Redirect(method = "method_27394", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;method_27521()Z", ordinal = 0))
    private boolean relightSkyLight(Dimension worldProvider, int x, int y, int z) {
        boolean hasSkylight = worldProvider.method_27521();
        if (!hasSkylight || !CarpetSettings.newLight) return hasSkylight;
        LightingHooks.relightSkylightColumn(world, (WorldChunk) (Object) this, x, z, this.field_25365 * 16 + x, this.field_25366 * 16 + z);
        return false; // cancel vanilla code
    }

    @Redirect(method = "method_27394", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;method_27521()Z", ordinal = 1))
    private boolean relightSkyLight2(Dimension worldProvider) {
        return !CarpetSettings.newLight && worldProvider.method_27521();
    }

    @Inject(method = "method_27364", at = @At("HEAD"))
    private void procOnGetLightFor(LightType type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.newLight) ((NewLightWorld) world).getLightingEngine().procLightUpdates(type);
    }

    @Inject(method = "method_27370", at = @At("HEAD"))
    private void procOnGetLightSubtracted(BlockPos pos, int amount, CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.newLight) ((NewLightWorld) world).getLightingEngine().procLightUpdates();
    }

    @Redirect(method = "method_27365", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;method_27385()V"))
    private void newLightGenerateSkylightMap(WorldChunk chunk, LightType type, BlockPos pos) {
        if (CarpetSettings.newLight) {
            //Forge: generateSkylightMap produces the wrong result (See #3870)
            LightingHooks.initSkylightForSection(world, chunk, sections[pos.getY() >> 4]);
        } else {
            chunk.method_27385();
        }
    }

    @Inject(method = "method_27392", at = @At("RETURN"))
    private void onOnLoad(CallbackInfo ci) {
        if (CarpetSettings.newLight) LightingHooks.onLoad(world, (WorldChunk) (Object) this);
    }

    @Redirect(method = "method_27367", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;method_27421()V"))
    private void checkLightInPopulate(WorldChunk chunk) {
        if (CarpetSettings.newLight) {
            this.field_25379 = true;
        } else {
            chunk.method_27421();
        }
    }

    @Redirect(method = "method_27391", at = @At(value = "FIELD", target = "Lnet/minecraft/world/chunk/WorldChunk;field_25380:Z"))
    private boolean checkLightInOnTick(WorldChunk chunk) {
        return CarpetSettings.newLight || field_25380;
    }


    @ModifyVariable(method = "method_27373", index = 12, at = @At(value = "STORE", ordinal = 1))
    private boolean setBlockStateInitSkylight(boolean flag, BlockPos pos) {
        if (CarpetSettings.newLight){
            //Forge: Always initialize sections properly (See #3870 and #3879)
            LightingHooks.initSkylightForSection(world, (WorldChunk) (Object) this, sections[pos.getY() >> 4]);
            //Forge: Don't call generateSkylightMap (as it produces the wrong result; sections are initialized above). Never bypass relightBlock (See #3870)
            return false;
        }
        return flag;
    }

    //Forge: Error correction is unnecessary as these are fixed (See #3871)
    @Redirect(method = "method_27373", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;method_27399(II)V"))
    private void dontPropagateSkylightOcclusion(WorldChunk chunk, int x, int z) {
        if (CarpetSettings.newLight) return;
        this.method_27399(x, z);
    }

    @Override
    public short[] getNeighborLightChecks() {
        return neighborLightChecks;
    }

    @Override
    public void setNeighborLightChecks(short[] checks) {
        neighborLightChecks = checks;
    }

    @Override
    public short getPendingNeighborLightInits() {
        return pendingNeighborLightInits;
    }

    @Override
    public void setPendingNeighborLightInits(int inits) {
        pendingNeighborLightInits = (short) inits;
    }

    @Override
    public int getCachedLightFor(LightType type, BlockPos pos)
    {
        int x = pos.getX() & 15;
        int y = pos.getY();
        int z = pos.getZ() & 15;
        ChunkSection section = this.sections[y >> 4];
        if (section == WorldChunk.EMPTY_SECTION) {
            return this.method_27396(pos) ? type.field_23634 : 0;
        }
        if (type == LightType.SKY) {
            return !this.world.dimension.method_27521() ? 0 : section.method_27440(x, y & 15, z);
        }
        return type == LightType.BLOCK ? section.method_27443(x, y & 15, z) : type.field_23634;
    }
}
