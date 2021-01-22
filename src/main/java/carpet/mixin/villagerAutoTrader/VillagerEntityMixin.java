package carpet.mixin.villagerAutoTrader;

import carpet.CarpetSettings;
import carpet.helpers.EntityAIAutotrader;
import carpet.utils.extensions.AutotraderVillagerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TraderOfferList;
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

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends PassiveEntity implements AutotraderVillagerEntity {
    @Shadow @Nullable private TraderOfferList offers;
    private EntityAIAutotrader autotraderAI;
    private TraderOfferList buyingListsorted;
    private final List<Integer> sortedTradeList = new LinkedList<>();

    public VillagerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;I)V", at = @At("RETURN"))
    private void onInit(World worldIn, int professionId, CallbackInfo ci) {
        autotraderAI = new EntityAIAutotrader((VillagerEntity) (Object) this);
    }



    @Inject(method = {
            "initGoals",
            "onGrowUp"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;getType()I"))
    private void addCraftTask(CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            goalSelector.add(6, autotraderAI);
        }
    }

    @Inject(method = "writeCustomDataToTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/TraderOfferList;toTag()Lnet/minecraft/nbt/CompoundTag;"))
    private void writeOffersSorted(CompoundTag compound, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            compound.put("OffersSorted", autotraderAI.getRecipiesForSaving(sortedTradeList));
        }
    }

    @Inject(method = "readCustomDataFromTag", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/passive/VillagerEntity;offers:Lnet/minecraft/village/TraderOfferList;", shift = At.Shift.AFTER))
    private void readOffersSorted(CompoundTag compound, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            autotraderAI.setRecipiesForSaving(compound.getCompound("OffersSorted"), sortedTradeList);
        }
    }

    @Inject(method = "setCurrentCustomer", at = @At("RETURN"))
    private void onSetCustomer(PlayerEntity player, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader && player != null) {
            autotraderAI.sortRepopulatedSortedList(offers, buyingListsorted, sortedTradeList);
        }
    }

    @Inject(method = "getTradeOffers", at = @At("RETURN"), cancellable = true)
    private void getRecipes(PlayerEntity player, CallbackInfoReturnable<TraderOfferList> cir) {
        if (CarpetSettings.villagerAutoTrader) {
            if (this.buyingListsorted == null) {
                buyingListsorted = new TraderOfferList();
                autotraderAI.sortRepopulatedSortedList(offers, buyingListsorted, sortedTradeList);
            } else if (buyingListsorted.size() == 0) {
                autotraderAI.sortRepopulatedSortedList(offers, buyingListsorted, sortedTradeList);
            }
            cir.setReturnValue(buyingListsorted);
        }
    }

    @Inject(method = "method_24926", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/passive/VillagerEntity;offers:Lnet/minecraft/village/TraderOfferList;", ordinal = 0))
    private void initBuyingListSorted(CallbackInfo ci) {
        if (this.buyingListsorted == null) {
            this.buyingListsorted = new TraderOfferList();
        }
    }

    @Inject(method = "method_24926", at = @At("RETURN"))
    private void onPopulateDone(CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader) {
            autotraderAI.sortRepopulatedSortedList(offers, buyingListsorted, sortedTradeList);
        }
    }

    @Inject(method = "loot", at = @At("RETURN"))
    private void onUpdateEquipment(ItemEntity itemEntity, CallbackInfo ci) {
        if (CarpetSettings.villagerAutoTrader && autotraderAI != null) {
            if (buyingListsorted == null) {
                buyingListsorted = new TraderOfferList();
                autotraderAI.sortRepopulatedSortedList(offers, buyingListsorted, sortedTradeList);
            }
            if (!itemEntity.removed) {
                autotraderAI.updateEquipment(itemEntity, buyingListsorted);
            }
        }
    }

    @Override
    public void addToFirstList(TradeOffer merchantrecipe) {
        if(!CarpetSettings.villagerAutoTrader) return;
        autotraderAI.addToFirstList(offers, merchantrecipe, sortedTradeList);
    }
}
