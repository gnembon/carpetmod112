package carpet.mixin.betterStatistics;

import carpet.CarpetServer;
import carpet.helpers.StatHelper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ScoreboardCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {
    @Redirect(method = "method_31845", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;addObjective(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardCriterion;)Lnet/minecraft/scoreboard/ScoreboardObjective;"))
    private ScoreboardObjective initalizeScores(Scoreboard scoreboard, String name, ScoreboardCriterion criteria) {
        ScoreboardObjective score = scoreboard.addObjective(name, criteria);
        StatHelper.initialize(scoreboard, CarpetServer.minecraft_server, score);
        return score;
    }
}
