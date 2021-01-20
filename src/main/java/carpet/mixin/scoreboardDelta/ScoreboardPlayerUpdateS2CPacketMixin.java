package carpet.mixin.scoreboardDelta;

import carpet.utils.extensions.ExtendedScore;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScoreboardPlayerUpdateS2CPacket.class)
public class ScoreboardPlayerUpdateS2CPacketMixin {
    @Redirect(method = "<init>(Lnet/minecraft/scoreboard/ScoreboardPlayerScore;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardPlayerScore;getScore()I"))
    private int getScorePoints(ScoreboardPlayerScore score) {
        return ((ExtendedScore) score).getScorePointsDelta();
    }
}
