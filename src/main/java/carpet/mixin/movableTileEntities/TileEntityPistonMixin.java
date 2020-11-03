package carpet.mixin.movableTileEntities;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedTileEntityPistonMTE;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityPiston.class)
public class TileEntityPistonMixin extends TileEntity implements ExtendedTileEntityPistonMTE {
    @Shadow private IBlockState pistonState;
    private TileEntity carriedTileEntity;


    @Redirect(method = "clearPistonTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getBlock()Lnet/minecraft/block/Block;"))
    private Block onClear(IBlockState state) {
        Block block = state.getBlock();
        if (!CarpetSettings.movableTileEntities && !CarpetSettings.autocrafter) return block;
        if (block == Blocks.PISTON_EXTENSION) {
            this.placeBlock();
            // cancel vanilla code
            return Blocks.AIR;
        }
        //Handle TNT Explosions or other ways the movingBlock is broken
        //Also /setblock will cause this to be called, and drop e.g. a moving chest's contents. This is MC-40380 (BlockEntities that aren't Inventories drop stuff when setblock is called )
        if (this.carriedTileEntity != null && this.world.getBlockState(this.pos).getBlock() == Blocks.AIR) {
            this.placeBlock();
            this.world.setBlockToAir(this.pos);
        }
        return block;
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"), cancellable = true)
    private void onFinished(CallbackInfo ci) {
        if(CarpetSettings.movableTileEntities || CarpetSettings.autocrafter) {
            this.placeBlock();
            ci.cancel();
        }
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void onDeserialize(NBTTagCompound compound, CallbackInfo ci) {
        if ((CarpetSettings.movableTileEntities || CarpetSettings.autocrafter) && compound.hasKey("carriedTileEntity", 10)) {
            if (this.pistonState.getBlock() instanceof ITileEntityProvider)
                this.carriedTileEntity = ((ITileEntityProvider) (this.pistonState.getBlock())).createNewTileEntity(this.world, this.pistonState.getBlock().getMetaFromState(this.pistonState));
            if (carriedTileEntity != null) //Can actually be null, as BlockPistonMoving.createNewTileEntity(...) returns null
                this.carriedTileEntity.readFromNBT(compound.getCompoundTag("carriedTileEntity"));
        }
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void onSerialize(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> cir) {
        if((CarpetSettings.movableTileEntities || CarpetSettings.autocrafter) && this.carriedTileEntity != null) {
            compound.setTag("carriedTileEntity", this.carriedTileEntity.writeToNBT(new NBTTagCompound()));
        }
    }

    public void setCarriedTileEntity(TileEntity tileEntity) {
        this.carriedTileEntity = tileEntity;
    }

    private void placeBlock() {
        this.world.setBlockState(this.pos, this.pistonState, 18); //Flag 18 => No block updates, TileEntity has to be placed first
        if (!this.world.isRemote) {
            if (carriedTileEntity != null) {
                this.world.removeTileEntity(this.pos);
                carriedTileEntity.validate();
                this.world.setTileEntity(this.pos, carriedTileEntity);
            }
            //Update neighbors, comparators and observers now (same order as setBlockState would have if flag was set to 3 (default))
            //This should not change piston behavior for vanilla-pushable blocks at all
            this.world.notifyNeighborsRespectDebug(pos, Blocks.PISTON_EXTENSION, true);
            if (this.pistonState.hasComparatorInputOverride()) {
                this.world.updateComparatorOutputLevel(pos, this.pistonState.getBlock());
            }
            this.world.updateObservingBlocksAt(pos, this.pistonState.getBlock());
        }
        this.world.neighborChanged(this.pos, this.pistonState.getBlock(), this.pos);
    }
}
