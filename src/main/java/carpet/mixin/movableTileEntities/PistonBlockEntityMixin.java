package carpet.mixin.movableTileEntities;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedPistonBlockEntityMBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBlockEntity.class)
public class PistonBlockEntityMixin extends BlockEntity implements ExtendedPistonBlockEntityMBE {
    @Shadow private BlockState pushedBlock;
    private BlockEntity carriedTileEntity;


    @Redirect(method = "finish", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
    private Block onClear(BlockState state) {
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
            this.world.removeBlock(this.pos);
        }
        return block;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), cancellable = true)
    private void onFinished(CallbackInfo ci) {
        if(CarpetSettings.movableTileEntities || CarpetSettings.autocrafter) {
            this.placeBlock();
            ci.cancel();
        }
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void onDeserialize(CompoundTag compound, CallbackInfo ci) {
        if ((CarpetSettings.movableTileEntities || CarpetSettings.autocrafter) && compound.contains("carriedTileEntity", 10)) {
            if (this.pushedBlock.getBlock() instanceof BlockEntityProvider)
                this.carriedTileEntity = ((BlockEntityProvider) (this.pushedBlock.getBlock())).createBlockEntity(this.world, this.pushedBlock.getBlock().getMeta(this.pushedBlock));
            if (carriedTileEntity != null) //Can actually be null, as BlockPistonMoving.createNewTileEntity(...) returns null
                this.carriedTileEntity.fromTag(compound.getCompound("carriedTileEntity"));
        }
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void onSerialize(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        if((CarpetSettings.movableTileEntities || CarpetSettings.autocrafter) && this.carriedTileEntity != null) {
            compound.put("carriedTileEntity", this.carriedTileEntity.toTag(new CompoundTag()));
        }
    }

    public void setCarriedTileEntity(BlockEntity tileEntity) {
        this.carriedTileEntity = tileEntity;
    }

    private void placeBlock() {
        this.world.setBlockState(this.pos, this.pushedBlock, 18); //Flag 18 => No block updates, TileEntity has to be placed first
        if (!this.world.isClient) {
            if (carriedTileEntity != null) {
                this.world.removeBlockEntity(this.pos);
                carriedTileEntity.cancelRemoval();
                this.world.setBlockEntity(this.pos, carriedTileEntity);
            }
            //Update neighbors, comparators and observers now (same order as setBlockState would have if flag was set to 3 (default))
            //This should not change piston behavior for vanilla-pushable blocks at all
            this.world.method_26017(pos, Blocks.PISTON_EXTENSION, true);
            if (this.pushedBlock.hasComparatorOutput()) {
                this.world.updateHorizontalAdjacent(pos, this.pushedBlock.getBlock());
            }
            this.world.method_26099(pos, this.pushedBlock.getBlock());
        }
        this.world.updateNeighbor(this.pos, this.pushedBlock.getBlock(), this.pos);
    }
}
