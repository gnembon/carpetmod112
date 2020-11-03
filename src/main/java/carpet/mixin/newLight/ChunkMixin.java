package carpet.mixin.newLight;

import carpet.CarpetSettings;
import carpet.helpers.LightingHooks;
import carpet.utils.extensions.NewLightChunk;
import carpet.utils.extensions.NewLightWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements NewLightChunk {
    private short[] neighborLightChecks = null;
    private short pendingNeighborLightInits;

    @Shadow @Final private World world;
    @Shadow @Final private ExtendedBlockStorage[] storageArrays;
    @Shadow private boolean isTerrainPopulated;
    @Shadow private boolean isLightPopulated;
    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow public abstract boolean canSeeSky(BlockPos pos);
    @Shadow public abstract void checkLight();
    @Shadow protected abstract void propagateSkylightOcclusion(int x, int z);

    @Inject(method = "generateSkylightMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;hasSkyLight()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void generateSkyLight(CallbackInfo ci, int top, int x, int z) {
        if (CarpetSettings.newLight) LightingHooks.fillSkylightColumn((Chunk) (Object) this, x, z);
    }

    @Redirect(method = "generateSkylightMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;hasSkyLight()Z"))
    private boolean generateSkyLightCancelVanilla(WorldProvider worldProvider) {
        return !CarpetSettings.newLight && worldProvider.hasSkyLight();
    }

    @ModifyConstant(method = "relightBlock", constant = @Constant(intValue = 255))
    private int noMask(int mask) {
        return CarpetSettings.newLight ? -1 : 255;
    }

    @Redirect(method = "relightBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;markBlocksDirtyVertical(IIII)V"))
    private void markDirtyInRelightBlock(World world, int x, int z, int y1, int y2) {
        if (!CarpetSettings.newLight) world.markBlocksDirtyVertical(x, z, y1, y2);
    }

    @Redirect(method = "relightBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;hasSkyLight()Z", ordinal = 0))
    private boolean relightSkyLight(WorldProvider worldProvider, int x, int y, int z) {
        boolean hasSkylight = worldProvider.hasSkyLight();
        if (!hasSkylight || !CarpetSettings.newLight) return hasSkylight;
        LightingHooks.relightSkylightColumn(world, (Chunk) (Object) this, x, z, this.x * 16 + x, this.z * 16 + z);
        return false; // cancel vanilla code
    }

    @Redirect(method = "relightBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;hasSkyLight()Z", ordinal = 1))
    private boolean relightSkyLight2(WorldProvider worldProvider) {
        return !CarpetSettings.newLight && worldProvider.hasSkyLight();
    }

    @Inject(method = "getLightFor", at = @At("HEAD"))
    private void procOnGetLightFor(EnumSkyBlock type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.newLight) ((NewLightWorld) world).getLightingEngine().procLightUpdates(type);
    }

    @Inject(method = "getLightSubtracted", at = @At("HEAD"))
    private void procOnGetLightSubtracted(BlockPos pos, int amount, CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.newLight) ((NewLightWorld) world).getLightingEngine().procLightUpdates();
    }

    @Redirect(method = "setLightFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;generateSkylightMap()V"))
    private void newLightGenerateSkylightMap(Chunk chunk, EnumSkyBlock type, BlockPos pos) {
        if (CarpetSettings.newLight) {
            //Forge: generateSkylightMap produces the wrong result (See #3870)
            LightingHooks.initSkylightForSection(world, chunk, storageArrays[pos.getY() >> 4]);
        } else {
            chunk.generateSkylightMap();
        }
    }

    @Inject(method = "onLoad", at = @At("RETURN"))
    private void onOnLoad(CallbackInfo ci) {
        if (CarpetSettings.newLight) LightingHooks.onLoad(world, (Chunk) (Object) this);
    }

    @Redirect(method = "populate(Lnet/minecraft/world/gen/IChunkGenerator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;checkLight()V"))
    private void checkLightInPopulate(Chunk chunk) {
        if (CarpetSettings.newLight) {
            this.isTerrainPopulated = true;
        } else {
            chunk.checkLight();
        }
    }

    @Redirect(method = "onTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/chunk/Chunk;isLightPopulated:Z"))
    private boolean checkLightInOnTick(Chunk chunk) {
        return CarpetSettings.newLight || isLightPopulated;
    }


    @ModifyVariable(method = "setBlockState", index = 12, at = @At(value = "STORE", ordinal = 1))
    private boolean setBlockStateInitSkylight(boolean flag, BlockPos pos) {
        if (CarpetSettings.newLight){
            //Forge: Always initialize sections properly (See #3870 and #3879)
            LightingHooks.initSkylightForSection(world, (Chunk) (Object) this, storageArrays[pos.getY() >> 4]);
            //Forge: Don't call generateSkylightMap (as it produces the wrong result; sections are initialized above). Never bypass relightBlock (See #3870)
            return false;
        }
        return flag;
    }

    //Forge: Error correction is unnecessary as these are fixed (See #3871)
    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;propagateSkylightOcclusion(II)V"))
    private void dontPropagateSkylightOcclusion(Chunk chunk, int x, int z) {
        if (CarpetSettings.newLight) return;
        this.propagateSkylightOcclusion(x, z);
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
    public int getCachedLightFor(EnumSkyBlock type, BlockPos pos)
    {
        int x = pos.getX() & 15;
        int y = pos.getY();
        int z = pos.getZ() & 15;
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[y >> 4];
        if (extendedblockstorage == Chunk.NULL_BLOCK_STORAGE) {
            return this.canSeeSky(pos) ? type.defaultLightValue : 0;
        }
        if (type == EnumSkyBlock.SKY) {
            return !this.world.provider.hasSkyLight() ? 0 : extendedblockstorage.getSkyLight(x, y & 15, z);
        }
        return type == EnumSkyBlock.BLOCK ? extendedblockstorage.getBlockLight(x, y & 15, z) : type.defaultLightValue;
    }
}
