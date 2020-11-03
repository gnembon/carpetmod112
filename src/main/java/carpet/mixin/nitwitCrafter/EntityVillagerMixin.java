package carpet.mixin.nitwitCrafter;

import carpet.CarpetSettings;
import carpet.helpers.EntityAICrafter;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityVillager.class)
public abstract class EntityVillagerMixin extends EntityAgeable {
    private EntityAICrafter craftingAI;

    @Shadow public abstract int getProfession();

    @Shadow @Final private InventoryBasic villagerInventory;

    public EntityVillagerMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;I)V", at = @At("RETURN"))
    private void onInit(World worldIn, int professionId, CallbackInfo ci) {
        craftingAI = new EntityAICrafter((EntityVillager) (Object) this);
    }

    @Inject(method = {
        "setAdditionalAItasks",
        "onGrowingAdult"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/EntityVillager;getProfession()I"))
    private void addCraftTask(CallbackInfo ci) {
        if (CarpetSettings.nitwitCrafter && getProfession() == 5) {
            craftingAI.updateNitwit();
            tasks.addTask(6, craftingAI);
        }
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityAgeable;onDeath(Lnet/minecraft/util/DamageSource;)V"))
    private void onDeath(DamageSource cause, CallbackInfo ci) {
        if (CarpetSettings.nitwitCrafter && getProfession() == 5 || CarpetSettings.villagerInventoryDropFix) {
            craftingAI.dropInventory();
        }
    }

    @Inject(method = "populateBuyingList", at = @At("HEAD"), cancellable = true)
    private void emptyNitwitBuyingList(CallbackInfo ci) {
        if (CarpetSettings.nitwitCrafter && getProfession() == 5) {
            ci.cancel();
        }
    }

    @Inject(method = "updateEquipmentIfNeeded", at = @At("HEAD"), cancellable = true)
    private void updateCraftingEquipment(EntityItem itemEntity, CallbackInfo ci) {
        if (CarpetSettings.nitwitCrafter && craftingAI != null) {
            if (craftingAI.updateEquipment(itemEntity, villagerInventory)) ci.cancel();
        }
    }
}
