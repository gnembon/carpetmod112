package carpet.helpers;

import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TraderOfferList;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

/**
 * Automatically trading villagers, the villager will pickup items from the ground and trade the items in the same order of the trade list.
 * The order of trades can be adjusted by trading in the open GUI ones where last trade is placed first.
 */
public class EntityAIAutotrader extends Goal {

    private VillagerEntity villager;
    private BlockPos emeraldBlockPosition = null;
    private int counter = 0;

    /**
     * Main constructor
     *
     * @param theVillagerIn the villager that is attached to the AI task.
     */
    public EntityAIAutotrader(VillagerEntity theVillagerIn) {
        this.villager = theVillagerIn;
    }

    /**
     * Should excecute for all AI tasks, always true here.
     *
     * @return
     */
    @Override
    public boolean canStart() {
        return true;
    }

    /**
     * AI update task that in this case only searches for a emerald block to throw items toward it when trading.
     */
    public void tick() {
        counter++;
        if (counter % 100 == 0) {
            findClosestEmeraldBlock();
        }
    }

    /**
     * Finds an emerald block the trader will use to throw the items towards.
     */
    private void findClosestEmeraldBlock() {
        World worldIn = villager.method_29608();
        BlockPos villagerpos = new BlockPos(villager);
        for (BlockPos pos : BlockPos.iterate(villagerpos.add(-3, -1, -3), villagerpos.add(3, 4, 3))) {
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
    public boolean updateEquipment(ItemEntity itemEntity, TraderOfferList merchantList) {
        for (TradeOffer merchantrecipe : merchantList) {
            if (!merchantrecipe.isDisabled()) {
                ItemStack groundItems = itemEntity.getStack();
                ItemStack buyItem = merchantrecipe.method_25834();
                if (groundItems.getItem() == buyItem.getItem()) {
                    int max = merchantrecipe.getMaxUses() - merchantrecipe.getUses();
                    int price = buyItem.getCount();
                    int gold = groundItems.getCount();
                    int count = gold / price;
                    if (count > max) {
                        count = max;
                    }

                    for (int i = 0; i < count; i++) {
                        villager.trade(merchantrecipe);
                        dropItem(merchantrecipe.method_25839().copy());
                        groundItems.decrement(price);
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

        float f1 = villager.headYaw;
        float f2 = villager.pitch;

        if (emeraldBlockPosition != null) {
            double d0 = emeraldBlockPosition.getX() + 0.5D - villager.field_33071;
            double d1 = emeraldBlockPosition.getY() + 1.5D - (villager.field_33072 + (double) villager.method_34518());
            double d2 = emeraldBlockPosition.getZ() + 0.5D - villager.field_33073;
            double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
            f1 = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
            f2 = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
        }

        double d0 = villager.field_33072 - 0.30000001192092896D + (double) villager.method_34518();
        ItemEntity entityitem = new ItemEntity(villager.world, villager.field_33071, d0, villager.field_33073, itemstack);
        float f = 0.3F;

        entityitem.field_33074 = -MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * f;
        entityitem.field_33075 = MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * f;
        entityitem.field_33076 = -MathHelper.sin(f2 * 0.017453292F) * 0.3F + 0.1F;
        entityitem.setToDefaultPickupDelay();
        villager.world.method_26040(entityitem);
    }

    /**
     * Adds the latest villager trade the player has trade to the a list that will be used to order the trades.
     *
     * @param buyingList
     * @param recipe
     * @param sortedTradeList
     */
    public void addToFirstList(TraderOfferList buyingList, TradeOffer recipe, List<Integer> sortedTradeList) {
        int index = -1;
        for (int i = 0; i < buyingList.size(); i++) {
            TradeOffer b = buyingList.get(i);
            if (b.method_25834().getItem().equals(recipe.method_25834().getItem()) && b.method_25839().getItem().equals(recipe.method_25839().getItem())) {
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
    public void sortRepopulatedSortedList(TraderOfferList buyingList, TraderOfferList buyingListsorted, List<Integer> sortedTradeList) {
        if(buyingList == null) return;

        TraderOfferList copy = new TraderOfferList();
        copy.addAll(buyingList);
        buyingListsorted.clear();
        for (int i : sortedTradeList) {
            TradeOffer r = copy.get(i);
            buyingListsorted.add(r);
        }
        for (TradeOffer r : buyingListsorted) {
            copy.remove(r);
        }
        for (TradeOffer r : copy) {
            buyingListsorted.add(r);
        }
    }

    /**
     * Reloads the NBT data of the sorted list for the trade order.
     *
     * @param nbttagcompound
     * @param sortedTradeList
     */
    public void setRecipiesForSaving(CompoundTag nbttagcompound, List<Integer> sortedTradeList) {
        ListTag nbttaglist = nbttagcompound.getList("Recipes", 10);

        for (int i = 0; i < nbttaglist.size(); ++i) {
            CompoundTag nbt = nbttaglist.getCompound(i);
            sortedTradeList.add(nbt.getInt("n"));
        }
    }

    /**
     * Saves the trade list into NBT for later saving to the villager
     *
     * @param list
     * @return
     */
    public CompoundTag getRecipiesForSaving(List<Integer> list) {
        CompoundTag nbttagcompound = new CompoundTag();
        ListTag nbttaglist = new ListTag();

        for (int i = 0; i < list.size(); ++i) {
            int index = list.get(i);
            CompoundTag num = new CompoundTag();
            num.putInt("n", index);
            nbttaglist.add(num);
        }

        nbttagcompound.put("Recipes", nbttaglist);
        return nbttagcompound;
    }
}
