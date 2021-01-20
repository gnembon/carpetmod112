package carpet.utils.extensions;

import net.minecraft.entity.effect.StatusEffectInstance;

public interface ExtendedStatusEffectInstance {
    StatusEffectInstance combine(StatusEffectInstance other);
    StatusEffectInstance getPrevious();
    void setPrevious(StatusEffectInstance previous);

    final class ItemPotionHolder {
        public static boolean itemPotion = false;
    }
}
