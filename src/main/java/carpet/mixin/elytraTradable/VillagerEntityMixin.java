package carpet.mixin.elytraTradable;

import carpet.CarpetSettings;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TraderOfferList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends PassiveEntity {
    @Shadow public abstract int method_24914();
    @Shadow private int field_22464;
    @Shadow private int field_22465;
    @Shadow private TraderOfferList field_22458;

    public VillagerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "method_24926", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/passive/VillagerEntity;field_22464:I", ordinal = 2))
    private void addElytra(CallbackInfo ci) {
        // leatherworker: profession=4, careerId=2
        if (CarpetSettings.elytraTradable && method_24914() == 4 && field_22464 == 2 && field_22465 == 4) {
            int leatherAmount = 15 + this.random.nextInt(64 - 15 + 1);
            int emeraldAmount = 20 + this.random.nextInt(64 - 20 + 1);
            this.field_22458.add(new TradeOffer(
                new ItemStack(Items.LEATHER, leatherAmount),
                new ItemStack(Items.EMERALD, emeraldAmount),
                new ItemStack(Items.ELYTRA)
            ));
        }
    }
}
