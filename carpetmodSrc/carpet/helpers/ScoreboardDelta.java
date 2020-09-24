package carpet.helpers;

import carpet.CarpetServer;
import carpet.utils.extensions.ExtendedScore;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;

import java.util.Collection;

/**
 * Class created for
 */
public class ScoreboardDelta {

    public static void update() {
        for(int i = 0; i < 2; i++) {
            ScoreObjective objective = CarpetServer.minecraft_server.getWorld(0).getScoreboard().getObjectiveInDisplaySlot(i);
            Collection<Score> list = CarpetServer.minecraft_server.getWorld(0).getScoreboard().getSortedScores(objective);

            for(Score s : list){
                ((ExtendedScore) s).computeScoreDelta();
                s.getScoreScoreboard().onScoreUpdated(s);
                if(((ExtendedScore) s).getScorePointsDelta() == 0){
                    s.getScoreScoreboard().broadcastScoreUpdate(s.getPlayerName());
                }
            }
        }
    }

    public static void resetScoreboardDelta(){
        for(int i = 0; i < 2; i++) {
            ScoreObjective objective = CarpetServer.minecraft_server.getWorld(0).getScoreboard().getObjectiveInDisplaySlot(i);
            Collection<Score> list = CarpetServer.minecraft_server.getWorld(0).getScoreboard().getSortedScores(objective);

            for(Score s : list){
                ((ExtendedScore) s).computeScoreDelta();
                s.getScoreScoreboard().onScoreUpdated(s);
            }
        }
    }
}
