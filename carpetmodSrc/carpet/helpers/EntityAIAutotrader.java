package carpet.helpers;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

public class EntityAIAutotrader extends EntityAIBase {

    private EntityVillager villager;
    private BlockPos emeraldBlockPosition = null;
    private int counter = 0;

    public EntityAIAutotrader(EntityVillager theVillagerIn) {
        this.villager = theVillagerIn;
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }

    public void updateTask() {
        counter++;
        if(counter % 100 == 0){
            findClosestEmeraldBlock();
        }
    }

    private void findClosestEmeraldBlock() {
        World worldIn = villager.getEntityWorld();
        BlockPos villagerpos = new BlockPos(villager);
        for (BlockPos pos : BlockPos.getAllInBox(villagerpos.add(-3, -1, -3), villagerpos.add(3, 4, 3))) {
            if (worldIn.getBlockState(pos).getBlock() == Blocks.EMERALD_BLOCK) {
                emeraldBlockPosition = pos;
                return;
            }
        }
        emeraldBlockPosition = null;
    }

    public boolean updateEquipment(EntityItem itemEntity, MerchantRecipeList merchantList) {
        for (MerchantRecipe merchantrecipe : merchantList) {
            if (!merchantrecipe.isRecipeDisabled()) {
                ItemStack groundItems = itemEntity.getItem();
                ItemStack buyItem = merchantrecipe.getItemToBuy();
                if (groundItems.getItem() == buyItem.getItem()) {
                    int max = merchantrecipe.getMaxTradeUses() - merchantrecipe.getToolUses();
                    int price = buyItem.getCount();
                    int gold = groundItems.getCount();
                    int count = gold / price;
                    if (count > max) {
                        count = max;
                    }

                    for (int i = 0; i < count; i++) {
                        villager.useRecipe(merchantrecipe);
                        dropItem(merchantrecipe.getItemToSell().copy());
                        groundItems.shrink(price);
                    }

                    return true;
                }
            }
        }
        return true;
    }

    private void dropItem(ItemStack itemstack) {
        if (itemstack.isEmpty())
            return;

        float f1 = villager.rotationYawHead;
        float f2 = villager.rotationPitch;

        if (emeraldBlockPosition != null) {
            double d0 = emeraldBlockPosition.getX() + 0.5D - villager.posX;
            double d1 = emeraldBlockPosition.getY() + 1.5D - (villager.posY + (double) villager.getEyeHeight());
            double d2 = emeraldBlockPosition.getZ() + 0.5D - villager.posZ;
            double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
            f1 = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
            f2 = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
        }

        double d0 = villager.posY - 0.30000001192092896D + (double) villager.getEyeHeight();
        EntityItem entityitem = new EntityItem(villager.world, villager.posX, d0, villager.posZ, itemstack);
        float f = 0.3F;

        entityitem.motionX = (double) (-MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * f);
        entityitem.motionZ = (double) (MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * f);
        entityitem.motionY = (double) (-MathHelper.sin(f2 * 0.017453292F) * 0.3F + 0.1F);
        entityitem.setDefaultPickupDelay();
        villager.world.spawnEntity(entityitem);
    }
}
