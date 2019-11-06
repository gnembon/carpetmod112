package carpet.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class provided by Skyrising ported to 1.12.2 by Xcom and DeadlyMC
 *
 * Auto crafting table tile entity class enabling autocrafting. When carpet rule enabled that tile
 * crafting table turns into a automatic crafting table where it can be used to automatically craft items.
 */
public class ContainerAutoCraftingTable extends ContainerWorkbench {
    private final TileEntityCraftingTable tileEntity;
    private final EntityPlayer player;

    ContainerAutoCraftingTable(InventoryPlayer playerInventory, TileEntityCraftingTable tileEntity, World world, BlockPos pos) {
        super(playerInventory, world, pos);
        this.tileEntity = tileEntity;
        this.player = playerInventory.player;
        inventorySlots.clear();
        this.addSlotToContainer(new OutputSlot(this.tileEntity));

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                this.addSlotToContainer(new Slot(this.tileEntity, x + y * 3 + 1, 30 + x * 18, 17 + y * 18));
            }
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv) {
        if (this.player instanceof EntityPlayerMP) {
            NetHandlerPlayServer netHandler = ((EntityPlayerMP) this.player).connection;
            netHandler.sendPacket(new SPacketSetSlot(this.windowId, 0, this.tileEntity.getStackInSlot(0)));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        if (slot == 0) {
            ItemStack before = this.tileEntity.getStackInSlot(0).copy();
            ItemStack current = before.copy();
            if (!this.mergeItemStack(current, 10, 46, true)) {
                return ItemStack.EMPTY;
            }
            tileEntity.setPlayer(player);
            this.tileEntity.decrStackSize(0, before.getCount() - current.getCount());
            tileEntity.setPlayer(null);
            return this.tileEntity.getStackInSlot(0);
        }
        return super.transferStackInSlot(player, slot);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        InventoryPlayer playerInventory = player.inventory;
        if (!playerInventory.getItemStack().isEmpty()) {
            player.dropItem(playerInventory.getItemStack(), false);
            playerInventory.setItemStack(ItemStack.EMPTY);
        }
        this.tileEntity.onContainerClose(this);
    }

    @Override
    public ItemStack decrStackSize(EntityPlayer player, int slotId, int amount){
        tileEntity.setPlayer(player);
        Slot slot = this.inventorySlots.get(slotId);
        ItemStack itemStack = slot.decrStackSize(amount);
        tileEntity.setPlayer(null);
        return itemStack;
    }

    private class OutputSlot extends Slot {
        OutputSlot(IInventory inv) {
            super(inv, 0, 124, 35);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        protected void onSwapCraft(int amount) {
            ContainerAutoCraftingTable.this.tileEntity.decrStackSize(0, amount);
        }
    }

    @Override
    public InventoryCrafting getInventoryCrafting() {
        return tileEntity.inventory;
    }
}