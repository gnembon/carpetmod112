package carpet.mixin.elytraTradable;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityVillager.class)
public abstract class EntityVillagerMixin extends EntityAgeable {
    @Shadow public abstract int getProfession();
    @Shadow private int careerId;
    @Shadow private int careerLevel;
    @Shadow private MerchantRecipeList buyingList;

    public EntityVillagerMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "populateBuyingList", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/passive/EntityVillager;careerId:I", ordinal = 2))
    private void addElytra(CallbackInfo ci) {
        // leatherworker: profession=4, careerId=2
        if (CarpetSettings.elytraTradable && getProfession() == 4 && careerId == 2 && careerLevel == 4) {
            int leatherAmount = 15 + this.rand.nextInt(64 - 15 + 1);
            int emeraldAmount = 20 + this.rand.nextInt(64 - 20 + 1);
            this.buyingList.add(new MerchantRecipe(
                new ItemStack(Items.LEATHER, leatherAmount),
                new ItemStack(Items.EMERALD, emeraldAmount),
                new ItemStack(Items.ELYTRA)
            ));
        }
    }
}
