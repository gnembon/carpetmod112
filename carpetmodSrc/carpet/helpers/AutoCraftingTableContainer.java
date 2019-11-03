package carpet.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AutoCraftingTableContainer extends ContainerWorkbench {
    private final CraftingTableBlockEntity blockEntity;
    private final EntityPlayer player;

    AutoCraftingTableContainer(InventoryPlayer playerInventory, CraftingTableBlockEntity blockEntity, World world, BlockPos pos) {
        super(playerInventory, world, pos);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
        inventorySlots.clear();
        this.addSlotToContainer(new OutputSlot(this.blockEntity));

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                this.addSlotToContainer(new Slot(this.blockEntity, x + y * 3 + 1, 30 + x * 18, 17 + y * 18));
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
            netHandler.sendPacket(new SPacketSetSlot(this.windowId, 0, this.blockEntity.getStackInSlot(0)));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        if (slot == 0) {
            ItemStack before = this.blockEntity.getStackInSlot(0).copy();
            ItemStack current = before.copy();
            if (!this.mergeItemStack(current, 10, 46, true)) {
                return ItemStack.EMPTY;
            }
            this.blockEntity.decrStackSize(0, before.getCount() - current.getCount());
            return this.blockEntity.getStackInSlot(0);
        }
        return super.transferStackInSlot(player, slot);
    }

    public void close(EntityPlayer player) {
        InventoryPlayer playerInventory = player.inventory;
        if (!playerInventory.getItemStack().isEmpty()) {
            player.dropItem(playerInventory.getItemStack(), false);
            playerInventory.setItemStack(ItemStack.EMPTY);
        }
        this.blockEntity.onContainerClose(this);
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
            AutoCraftingTableContainer.this.blockEntity.decrStackSize(0, amount);
        }
    }
}