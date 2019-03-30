package carpet.helpers;

import java.util.List;

import com.google.common.base.Predicates;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.fixes.MinecartEntityTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/*
 * Dispenser addons to improve dispensers dispense more stuff.
 * add this to end of dispenser section of bootstrap and fix the TNT part by removing it.
 * 
 *      // Carpet Dispenser addons XCOM-CARPET
        BetterDispenser.dispenserAddons();
 */

public class BetterDispenser {
    
    public static void dispenserAddons(){
        // Block rotation stuffs CARPET-XCOM
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(Blocks.CACTUS), new Bootstrap.BehaviorDispenseOptional()
        {
            private final BehaviorDefaultDispenseItem dispenseBehavior = new BehaviorDefaultDispenseItem();
            
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                if(!CarpetSettings.rotatorBlock){
                    return this.dispenseBehavior.dispense(source, stack);
                }
                EnumFacing sourceFace = (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING);
                World world = source.getWorld();
                BlockPos blockpos = source.getBlockPos().offset(sourceFace);
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();
                
                // Block rotation for blocks that can be placed in all 6 rotations.
                if(block instanceof BlockDirectional || block instanceof BlockDispenser){ 
                    EnumFacing face = (EnumFacing)iblockstate.getValue(BlockDirectional.FACING);
                    face = face.rotateAround(sourceFace.getAxis());
                    if(sourceFace.getIndex() % 2 == 0){ // Rotate twice more to make blocks always rotate clockwise relative to the dispenser
                                                        // when index is equal to zero. when index is equal to zero the dispenser is in the opposite direction.
                        face = face.rotateAround(sourceFace.getAxis());
                        face = face.rotateAround(sourceFace.getAxis());
                    }
                    world.setBlockState(blockpos, iblockstate.withProperty(BlockDirectional.FACING, face), 3);
                
                // Block rotation for blocks that can be placed in only 4 horizontal rotations.
                }else if(block instanceof BlockHorizontal){
                    EnumFacing face = (EnumFacing)iblockstate.getValue(BlockHorizontal.FACING);
                    face = face.rotateAround(sourceFace.getAxis());
                    if(sourceFace.getIndex() % 2 == 0){ // same as above.
                        face = face.rotateAround(sourceFace.getAxis());
                        face = face.rotateAround(sourceFace.getAxis());
                    }
                    if(sourceFace.getIndex() <= 1){ // Make sure to suppress rotation when index is lower then 2 as that will result in a faulty rotation for 
                                                    // blocks that only can be placed horizontaly.
                        world.setBlockState(blockpos, iblockstate.withProperty(BlockHorizontal.FACING, face), 3);
                    }
                }
                // Send block update to the block that just have been rotated.
                world.neighborChanged(blockpos, block, source.getBlockPos());
                
                return stack;
            }
        });
        
        // Block fill bottle of water. XCOM-CARPET
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.GLASS_BOTTLE, new BehaviorDefaultDispenseItem()
        {
            private final BehaviorDefaultDispenseItem dispenseBehavior = new BehaviorDefaultDispenseItem();
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                if(!CarpetSettings.dispenserWaterBottle){
                    return this.dispenseBehavior.dispense(source, stack);
                }
                
                World world = source.getWorld();
                BlockPos blockpos = source.getBlockPos().offset((EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING));
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();
                Material material = iblockstate.getMaterial();
                ItemStack itemstack;

                if (Material.WATER.equals(material) && block instanceof BlockLiquid && ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0)
                {
                    itemstack = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
                }
                else
                {
                    itemstack = new ItemStack(Items.GLASS_BOTTLE);
                }

                stack.shrink(1);

                if (stack.isEmpty())
                {
                    return itemstack;
                }
                else
                {
                    if (((TileEntityDispenser)source.getBlockTileEntity()).addItemStack(itemstack) < 0)
                    {
                        this.dispenseBehavior.dispense(source, itemstack);
                    }

                    return stack;
                }
            }
        });
        
        // Chest/hopper/tnt/furnnace Minecart thingy XCOM-CARPET
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(Blocks.CHEST), new BehaviorDispenseMinecart(EntityMinecart.Type.CHEST));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(Blocks.HOPPER), new BehaviorDispenseMinecart(EntityMinecart.Type.HOPPER));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(Blocks.FURNACE), new BehaviorDispenseMinecart(EntityMinecart.Type.FURNACE));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(Blocks.TNT), new BehaviorDispenseMinecart(EntityMinecart.Type.TNT));
        /*
         * for tnt use this in the already existing tnt code if the removal isnt used.
         *      Bootstrap.BehaviorDispenseMinecart tntDispense = new Bootstrap.BehaviorDispenseMinecart(EntityMinecart.Type.TNT);
         *      return tntDispense.dispense(source, stack);
         */

    }
    
    public static class BehaviorDispenseMinecart extends BehaviorDefaultDispenseItem
    {
        private final BehaviorDefaultDispenseItem dispenseBehavior = new BehaviorDefaultDispenseItem();
        private final EntityMinecart.Type minecartType;

        public BehaviorDispenseMinecart(EntityMinecart.Type type)
        {
            this.minecartType = type;
        }

        public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
        {
            if(!CarpetSettings.dispenserMinecartFiller){
                return defaultBehavior(source, stack);
            }
            
            BlockPos blockpos = source.getBlockPos().offset((EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING));
            List<EntityMinecartEmpty> list = source.getWorld().<EntityMinecartEmpty>getEntitiesWithinAABB(EntityMinecartEmpty.class, new AxisAlignedBB(blockpos) );
    
            if (list.isEmpty())
            {
                return defaultBehavior(source, stack);
            }
            else
            {
                EntityMinecartEmpty minecart = list.get(0);
                minecart.setDead();
                EntityMinecart entityminecart = EntityMinecart.create(minecart.world, minecart.posX, minecart.posY, minecart.posZ, this.minecartType);
                entityminecart.motionX = minecart.motionX;
                entityminecart.motionY = minecart.motionY;
                entityminecart.motionZ = minecart.motionZ;
                entityminecart.rotationPitch = minecart.rotationPitch;
                entityminecart.rotationYaw = minecart.rotationYaw;
                
                minecart.world.spawnEntity(entityminecart);
                stack.shrink(1);
                return stack;
            }
        }
        
        private ItemStack defaultBehavior(IBlockSource source, ItemStack stack){
            if(this.minecartType == EntityMinecart.Type.TNT){
                World world = source.getWorld();
                BlockPos blockpos = source.getBlockPos().offset((EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING));
                EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(world, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, (EntityLivingBase)null);
                world.spawnEntity(entitytntprimed);
                world.playSound((EntityPlayer)null, entitytntprimed.posX, entitytntprimed.posY, entitytntprimed.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                stack.shrink(1);
                return stack;
            }else{
                return this.dispenseBehavior.dispense(source, stack);
            }
        }

        protected void playDispenseSound(IBlockSource source)
        {
            source.getWorld().playEvent(1000, source.getBlockPos(), 0);
        }
    }
}
