package carpet.helpers;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

/**
 * Automatically trading villagers, the villager will pickup items from the ground and trade the items in the same order of the trade list.
 * The order of trades can be adjusted by trading in the open GUI ones where last trade is placed first.
 */
public class EntityAIAutotrader extends EntityAIBase {

    private EntityVillager villager;
    private BlockPos emeraldBlockPosition = null;
    private int counter = 0;

    /**
     * Main constructor
     *
     * @param theVillagerIn the villager that is attached to the AI task.
     */
    public EntityAIAutotrader(EntityVillager theVillagerIn) {
        this.villager = theVillagerIn;
    }

    /**
     * Should excecute for all AI tasks, always true here.
     *
     * @return
     */
    @Override
    public boolean shouldExecute() {
        return true;
    }

    /**
     * AI update task that in this case only searches for a emerald block to throw items toward it when trading.
     */
    public void updateTask() {
        counter++;
        if (counter % 100 == 0) {
            findClosestEmeraldBlock();
        }
    }

    /**
     * Finds an emerald block the trader will use to throw the items towards.
     */
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

    /**
     * Update task that will make the villager search for items on the ground and throw items towards.
     *
     * @param itemEntity
     * @param merchantList
     * @return
     */
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

    /**
     * Drop item call when the villager succesfully finds an item to trade.
     *
     * @param itemstack
     */
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

    /**
     * Adds the latest villager trade the player has trade to the a list that will be used to order the trades.
     *
     * @param buyingList
     * @param recipe
     * @param sortedTradeList
     */
    public void addToFirstList(MerchantRecipeList buyingList, MerchantRecipe recipe, List<Integer> sortedTradeList) {
        int index = -1;
        for (int i = 0; i < buyingList.size(); i++) {
            MerchantRecipe b = buyingList.get(i);
            if (b.getItemToBuy().getItem().equals(recipe.getItemToBuy().getItem()) && b.getItemToSell().getItem().equals(recipe.getItemToSell().getItem())) {
                index = i;
                break;
            }
        }
        if (index == -1) return;
        Iterator<Integer> iter = sortedTradeList.iterator();
        while (iter.hasNext()) {
            int i = iter.next();
            if (i == index) {
                iter.remove();
                break;
            }
        }
        sortedTradeList.add(0, index);
    }

    /**
     * Sortes the villager trades differently then in vanilla with the last trade first.
     *
     * @param buyingList
     * @param buyingListsorted
     * @param sortedTradeList
     */
    public void sortRepopulatedSortedList(MerchantRecipeList buyingList, MerchantRecipeList buyingListsorted, List<Integer> sortedTradeList) {
        if(buyingList == null) return;

        MerchantRecipeList copy = new MerchantRecipeList();
        copy.addAll(buyingList);
        buyingListsorted.clear();
        for (int i : sortedTradeList) {
            MerchantRecipe r = copy.get(i);
            buyingListsorted.add(r);
        }
        for (MerchantRecipe r : buyingListsorted) {
            copy.remove(r);
        }
        for (MerchantRecipe r : copy) {
            buyingListsorted.add(r);
        }
    }

    /**
     * Reloads the NBT data of the sorted list for the trade order.
     *
     * @param nbttagcompound
     * @param sortedTradeList
     */
    public void setRecipiesForSaving(NBTTagCompound nbttagcompound, List<Integer> sortedTradeList) {
        NBTTagList nbttaglist = nbttagcompound.getTagList("Recipes", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbt = nbttaglist.getCompoundTagAt(i);
            sortedTradeList.add(nbt.getInteger("n"));
        }
    }

    /**
     * Saves the trade list into NBT for later saving to the villager
     *
     * @param list
     * @return
     */
    public NBTTagCompound getRecipiesForSaving(List<Integer> list) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < list.size(); ++i) {
            int index = list.get(i);
            NBTTagCompound num = new NBTTagCompound();
            num.setInteger("n", index);
            nbttaglist.appendTag(num);
        }

        nbttagcompound.setTag("Recipes", nbttaglist);
        return nbttagcompound;
    }
}
