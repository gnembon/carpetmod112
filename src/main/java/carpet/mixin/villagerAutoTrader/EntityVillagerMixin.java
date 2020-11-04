package carpet.mixin.villagerAutoTrader;

import carpet.CarpetSettings;
import carpet.helpers.EntityAIAutotrader;
import carpet.utils.extensions.ExtendedEntityVillagerAutotrader;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

@Mixin(EntityVillager.class)
public abstract class EntityVillagerMixin extends EntityAgeable implements ExtendedEntityVillagerAutotrader {
    @Shadow @Nullable private MerchantRecipeList buyingList;
    private EntityAIAutotrader autotraderAI;
    private MerchantRecipeList buyingListsorted;
    private final List<Integer> sortedTradeList = new LinkedList<>();

    public EntityVillagerMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;I)V", at = @At("RETURN"))
    private void onInit(World worldIn, int professionId, CallbackInfo ci) {
        autotraderAI = new EntityAIAutotrader((EntityVillager) (Object) this);
    }



    @Inject(method = {
            "setAdditionalAItasks",
            "onGrowingAdult"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/EntityVillager;getProfession()I"))
    private void addCraftTask(CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            tasks.addTask(6, autotraderAI);
        }
    }

    @Inject(method = "writeEntityToNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/MerchantRecipeList;getRecipiesAsTags()Lnet/minecraft/nbt/NBTTagCompound;"))
    private void writeOffersSorted(NBTTagCompound compound, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            compound.setTag("OffersSorted", autotraderAI.getRecipiesForSaving(sortedTradeList));
        }
    }

    @Inject(method = "readEntityFromNBT", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/passive/EntityVillager;buyingList:Lnet/minecraft/village/MerchantRecipeList;", shift = At.Shift.AFTER))
    private void readOffersSorted(NBTTagCompound compound, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            autotraderAI.setRecipiesForSaving(compound.getCompoundTag("OffersSorted"), sortedTradeList);
        }
    }

    @Inject(method = "setCustomer", at = @At("RETURN"))
    private void onSetCustomer(EntityPlayer player, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader && player != null) {
            autotraderAI.sortRepopulatedSortedList(buyingList, buyingListsorted, sortedTradeList);
        }
    }

    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)
    private void getRecipes(EntityPlayer player, CallbackInfoReturnable<MerchantRecipeList> cir) {
        if (CarpetSettings.villagerAutoTrader) {
            if (this.buyingListsorted == null) {
                buyingListsorted = new MerchantRecipeList();
                autotraderAI.sortRepopulatedSortedList(buyingList, buyingListsorted, sortedTradeList);
            } else if (buyingListsorted.size() == 0) {
                autotraderAI.sortRepopulatedSortedList(buyingList, buyingListsorted, sortedTradeList);
            }
            cir.setReturnValue(buyingListsorted);
        }
    }

    @Inject(method = "populateBuyingList", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/passive/EntityVillager;buyingList:Lnet/minecraft/village/MerchantRecipeList;", ordinal = 0))
    private void initBuyingListSorted(CallbackInfo ci) {
        if (this.buyingListsorted == null) {
            this.buyingListsorted = new MerchantRecipeList();
        }
    }

    @Inject(method = "populateBuyingList", at = @At("RETURN"))
    private void onPopulateDone(CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            autotraderAI.sortRepopulatedSortedList(buyingList, buyingListsorted, sortedTradeList);
        }
    }

    @Inject(method = "updateEquipmentIfNeeded", at = @At("RETURN"))
    private void onUpdateEquipment(EntityItem itemEntity, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader && autotraderAI != null) {
            if (buyingListsorted == null) {
                buyingListsorted = new MerchantRecipeList();
                autotraderAI.sortRepopulatedSortedList(buyingList, buyingListsorted, sortedTradeList);
            }
            if (!itemEntity.isDead) {
                autotraderAI.updateEquipment(itemEntity, buyingListsorted);
            }
        }
    }

    @Override
    public void addToFirstList(MerchantRecipe merchantrecipe) {
        if(!CarpetSettings.villagerAutoTrader) return;
        autotraderAI.addToFirstList(buyingList, merchantrecipe, sortedTradeList);
    }
}
