package carpet.mixin.potions;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedPotionEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PotionEffect.class)
public class PotionEffectMixin implements ExtendedPotionEffect {
    @Shadow @Final private Potion potion;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private boolean isAmbient;
    @Shadow private int amplifier;
    @Shadow private int duration;
    @Shadow private boolean showParticles;

    private PotionEffect previous;

    @Override
    @SuppressWarnings("ConstantConditions")
    public PotionEffect combine(PotionEffect other) {
        if (this.potion != other.getPotion()) LOGGER.warn("This method should only be called for matching effects!");
        if (other == (Object) this) return other;
        boolean combine = CarpetSettings.combinePotionDuration > 0 && ItemPotionHolder.itemPotion && other.getAmplifier() == this.amplifier;
        if (!combine && CarpetSettings.effectsFix && !this.isAmbient && other.getAmplifier() >= this.amplifier && other.getDuration() < this.duration) {
            boolean stack = true;
            for (PotionEffect e = other; e != null; e = ((ExtendedPotionEffect) e).getPrevious()) {
                if (e == (Object) this) {
                    LOGGER.warn("Tried to recursively combine effects " + this + " and " + other);
                    stack = false;
                    break;
                }
            }
            if (stack) {
                ((ExtendedPotionEffect) other).setPrevious((PotionEffect) (Object) this);
                return other;
            }
        }

        if (other.getAmplifier() > this.amplifier) {
            this.amplifier = other.getAmplifier();
            this.duration = other.getDuration();
        }
        // Combines the potion durations of identical potions when a player drinks the potions. CARPET-XCOM
        else if (combine) {
            if (this.duration < 0) this.duration = 0;
            this.duration += other.getDuration();
            // Cap the duration to the carpet rule amount. Also make sure its more then the amount being added.
            if (this.duration > CarpetSettings.combinePotionDuration && CarpetSettings.combinePotionDuration > other.getDuration()) {
                this.duration = CarpetSettings.combinePotionDuration;
            }
        } else if (other.getAmplifier() == this.amplifier && this.duration < other.getDuration()) {
            this.duration = other.getDuration();
        } else if (!other.getIsAmbient() && this.isAmbient) {
            this.isAmbient = other.getIsAmbient();
        }

        this.showParticles = other.doesShowParticles();
        return (PotionEffect) (Object) this;
    }

    @Override
    public PotionEffect getPrevious() {
        return previous;
    }

    @Override
    public void setPrevious(PotionEffect previous) {
        this.previous = previous;
    }
}
