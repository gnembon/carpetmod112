package carpet.helpers;

import net.minecraft.container.CraftingTableContainer;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class provided by Skyrising ported to 1.12.2 by Xcom and DeadlyMC
 *
 * Auto crafting table tile entity class enabling autocrafting. When carpet rule enabled that tile
 * crafting table turns into a automatic crafting table where it can be used to automatically craft items.
 */
public class ContainerAutoCraftingTable extends CraftingTableContainer {
    private final CraftingTableBlockEntity tileEntity;
    private final PlayerEntity player;

    ContainerAutoCraftingTable(PlayerInventory playerInventory, CraftingTableBlockEntity tileEntity, World world, BlockPos pos) {
        super(playerInventory, world, pos);
        this.tileEntity = tileEntity;
        this.player = playerInventory.player;
        field_22767.clear();
        this.addSlot(new OutputSlot(this.tileEntity));

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                this.addSlot(new Slot(this.tileEntity, x + y * 3 + 1, 30 + x * 18, 17 + y * 18));
            }
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    public ItemStack onSlotClick(int slotId, int dragType, SlotActionType clickTypeIn, PlayerEntity player) {
        try {
            tileEntity.setPlayer(player);
            return super.onSlotClick(slotId, dragType, clickTypeIn, player);
        } finally {
            tileEntity.setPlayer(null);
        }
    }

    @Override
    public void onContentChanged(Inventory inv) {
        if (this.player instanceof ServerPlayerEntity) {
            ServerPlayNetworkHandler netHandler = ((ServerPlayerEntity) this.player).networkHandler;
            netHandler.method_33624(new ContainerSlotUpdateS2CPacket(this.field_22768, 0, this.tileEntity.getInvStack(0)));
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int slot) {
        if (slot == 0) {
            ItemStack before = this.tileEntity.getInvStack(0).copy();
            ItemStack current = before.copy();
            if (!this.insertItem(current, 10, 46, true)) {
                return ItemStack.EMPTY;
            }
            tileEntity.setPlayer(player);
            this.tileEntity.takeInvStack(0, before.getCount() - current.getCount());
            tileEntity.setPlayer(null);
            return this.tileEntity.getInvStack(0);
        }
        return super.transferSlot(player, slot);
    }

    @Override
    public void close(PlayerEntity player) {
        PlayerInventory playerInventory = player.inventory;
        if (!playerInventory.getCursorStack().isEmpty()) {
            player.dropItem(playerInventory.getCursorStack(), false);
            playerInventory.setCursorStack(ItemStack.EMPTY);
        }
        this.tileEntity.onContainerClose(this);
    }

    private class OutputSlot extends Slot {
        OutputSlot(Inventory inv) {
            super(inv, 0, 124, 35);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        protected void onTake(int amount) {
            ContainerAutoCraftingTable.this.tileEntity.takeInvStack(0, amount);
        }
    }

    public CraftingInventory getInventoryCrafting() {
        return tileEntity.inventory;
    }
}