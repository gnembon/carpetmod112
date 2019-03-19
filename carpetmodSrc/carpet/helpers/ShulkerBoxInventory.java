package carpet.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;

public class ShulkerBoxInventory extends TileEntityShulkerBox {
    private final ItemStack stack;
    private final NBTTagCompound blockEntityTag;

    public ShulkerBoxInventory(ItemStack stack) {
        this.stack = stack;
        this.blockEntityTag = stack.getOrCreateSubCompound("BlockEntityTag");
    }

    @Override
    public void openInventory(EntityPlayer player) {
        this.loadFromNbt(this.blockEntityTag);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        ItemStackHelper.saveAllItems(this.blockEntityTag, this.getItems());
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.dropItem(stack, false);
        } else {
            ((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return !player.isSpectator();
    }
}
