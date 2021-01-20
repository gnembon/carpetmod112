package carpet.helpers;

import carpet.CarpetServer;
import carpet.utils.extensions.ExtendedScore;
import java.util.Collection;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;

/**
 * Class created for
 */
public class ScoreboardDelta {

    public static void update() {
        for(int i = 0; i < 2; i++) {
            ScoreboardObjective objective = CarpetServer.minecraft_server.getWorldById(0).method_26057().getObjectiveForSlot(i);
            Collection<ScoreboardPlayerScore> list = CarpetServer.minecraft_server.getWorldById(0).method_26057().getAllPlayerScores(objective);

            for(ScoreboardPlayerScore s : list){
                ((ExtendedScore) s).computeScoreDelta();
                s.getScoreboard().updateScore(s);
                if(((ExtendedScore) s).getScorePointsDelta() == 0){
                    s.getScoreboard().updatePlayerScore(s.getPlayerName());
                }
            }
        }
    }

    public static void resetScoreboardDelta(){
        for(int i = 0; i < 2; i++) {
            ScoreboardObjective objective = CarpetServer.minecraft_server.getWorldById(0).method_26057().getObjectiveForSlot(i);
            Collection<ScoreboardPlayerScore> list = CarpetServer.minecraft_server.getWorldById(0).method_26057().getAllPlayerScores(objective);

            for(ScoreboardPlayerScore s : list){
                ((ExtendedScore) s).computeScoreDelta();
                s.getScoreboard().updateScore(s);
            }
        }
    }
}
