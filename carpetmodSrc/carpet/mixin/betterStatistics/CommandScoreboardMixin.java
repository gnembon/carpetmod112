package carpet.mixin.betterStatistics;

import carpet.CarpetServer;
import carpet.helpers.StatHelper;
import net.minecraft.command.server.CommandScoreboard;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandScoreboard.class)
public class CommandScoreboardMixin {
    @Redirect(method = "addObjective", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;addScoreObjective(Ljava/lang/String;Lnet/minecraft/scoreboard/IScoreCriteria;)Lnet/minecraft/scoreboard/ScoreObjective;"))
    private ScoreObjective initalizeScores(Scoreboard scoreboard, String name, IScoreCriteria criteria) {
        ScoreObjective score = scoreboard.addScoreObjective(name, criteria);
        StatHelper.initialize(scoreboard, CarpetServer.minecraft_server, score);
        return score;
    }
}
