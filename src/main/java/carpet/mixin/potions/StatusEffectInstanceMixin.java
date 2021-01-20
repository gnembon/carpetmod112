package carpet.mixin.potions;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedStatusEffectInstance;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin implements ExtendedStatusEffectInstance {
    @Shadow @Final private StatusEffect type;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private boolean ambient;
    @Shadow private int amplifier;
    @Shadow private int duration;
    @Shadow private boolean field_32943;

    private StatusEffectInstance previous;

    @Override
    @SuppressWarnings("ConstantConditions")
    public StatusEffectInstance combine(StatusEffectInstance other) {
        if (this.type != other.getEffectType()) LOGGER.warn("This method should only be called for matching effects!");
        if (other == (Object) this) return other;
        boolean combine = CarpetSettings.combinePotionDuration > 0 && ItemPotionHolder.itemPotion && other.getAmplifier() == this.amplifier;
        if (!combine && CarpetSettings.effectsFix && !this.ambient && other.getAmplifier() >= this.amplifier && other.getDuration() < this.duration) {
            boolean stack = true;
            for (StatusEffectInstance e = other; e != null; e = ((ExtendedStatusEffectInstance) e).getPrevious()) {
                if (e == (Object) this) {
                    LOGGER.warn("Tried to recursively combine effects " + this + " and " + other);
                    stack = false;
                    break;
                }
            }
            if (stack) {
                ((ExtendedStatusEffectInstance) other).setPrevious((StatusEffectInstance) (Object) this);
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
        } else if (!other.isAmbient() && this.ambient) {
            this.ambient = other.isAmbient();
        }

        this.field_32943 = other.shouldShowParticles();
        return (StatusEffectInstance) (Object) this;
    }

    @Override
    public StatusEffectInstance getPrevious() {
        return previous;
    }

    @Override
    public void setPrevious(StatusEffectInstance previous) {
        this.previous = previous;
    }
}
