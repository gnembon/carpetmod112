package carpet.utils.extensions;

import net.minecraft.potion.PotionEffect;

public interface ExtendedPotionEffect {
    PotionEffect combine(PotionEffect other);
    PotionEffect getPrevious();
    void setPrevious(PotionEffect previous);

    final class ItemPotionHolder {
        public static boolean itemPotion = false;
    }
}
