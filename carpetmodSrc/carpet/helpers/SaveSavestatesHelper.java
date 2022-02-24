package carpet.helpers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class SaveSavestatesHelper {
    public static void trySaveItemsCompressed(NBTTagCompound destTag, NonNullList<ItemStack> items, boolean saveEmpty) {
        List<Pair<Integer, ItemStack>> itemsAndSlots = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            itemsAndSlots.add(Pair.of(i, items.get(i)));
        }

        itemsAndSlots.sort((a, b) -> compareItems(a.getRight(), b.getRight()));

        NBTTagList itemsTag = new NBTTagList();
        for (Pair<Integer, ItemStack> itemAndSlot : itemsAndSlots) {
            int slot = itemAndSlot.getLeft();
            ItemStack item = itemAndSlot.getRight();
            if (!item.isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) slot);
                item.writeToNBT(itemTag);
                itemsTag.appendTag(itemTag);
            }
        }

        if (!itemsTag.isEmpty() || saveEmpty) {
            destTag.setTag("Items", itemsTag);
        }
    }

    private static int compareItems(ItemStack a, ItemStack b) {
        int idA = Item.getIdFromItem(a.getItem());
        int idB = Item.getIdFromItem(b.getItem());
        if (idA != idB) {
            return Integer.compare(idA, idB);
        }

        NBTTagCompound tagA = a.getTagCompound();
        NBTTagCompound tagB = b.getTagCompound();
        if (tagA != null && tagB != null) {
            NBTTagList pagesA = tagA.getTagList("pages", 8);
            NBTTagList pagesB = tagB.getTagList("pages", 8);
            for (int page = 0; page < Math.min(pagesA.tagCount(), pagesB.tagCount()); page++) {
                String pageA = pagesA.getStringTagAt(page);
                String pageB = pagesB.getStringTagAt(page);
                int cmp = pageA.compareTo(pageB);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }

        return 0;
    }
}
