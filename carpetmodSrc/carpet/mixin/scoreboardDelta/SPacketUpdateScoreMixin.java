package carpet.mixin.scoreboardDelta;

import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.scoreboard.Score;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SPacketUpdateScore.class)
public class SPacketUpdateScoreMixin {
    @Redirect(method = "<init>(Lnet/minecraft/scoreboard/Score;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Score;getScorePoints()I"))
    private int getScorePoints(Score score) {
        return score.getScorePointsDelta();
    }
}
