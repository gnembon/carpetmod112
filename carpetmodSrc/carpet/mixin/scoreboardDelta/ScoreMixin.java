package carpet.mixin.scoreboardDelta;

import carpet.CarpetSettings;
import carpet.utils.extensions.ExtendedScore;
import net.minecraft.scoreboard.Score;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Mixin(Score.class)
public class ScoreMixin implements ExtendedScore {
    @Shadow public static @Final @Mutable Comparator<Score> SCORE_COMPARATOR = (a, b) -> {
        int comparePoint = Integer.compare(((ExtendedScore) a).getScorePointsDelta(), ((ExtendedScore) b).getScorePointsDelta());
        return comparePoint == 0 ? b.getPlayerName().compareToIgnoreCase(a.getPlayerName()) : comparePoint;
    };
    @Shadow private int scorePoints;
    private int scorePointsDelta;
    private final List<Pair<Long, Integer>> list = new LinkedList<>();

    @Inject(method = "setScorePoints", at = @At("HEAD"))
    private void updateDelta(int points, CallbackInfo ci) {
        if (CarpetSettings.scoreboardDelta > 0) {
            list.add(Pair.of(System.currentTimeMillis(), points));
            computeScoreDelta();
        }
    }

    @Override
    public int getScorePointsDelta() {
        return CarpetSettings.scoreboardDelta > 0 ? scorePointsDelta : scorePoints;
    }

    @Override
    public void computeScoreDelta() {
        int oldest = Integer.MIN_VALUE;
        Iterator<Pair<Long, Integer>> iter = list.iterator();
        while (iter.hasNext()) {
            Pair<Long, Integer> p = iter.next();
            if (p.getKey() > (System.currentTimeMillis() - CarpetSettings.scoreboardDelta * 1000)) {
                oldest = p.getValue();
                break;
            } else {
                iter.remove();
            }
        }
        if (oldest != Integer.MIN_VALUE) {
            scorePointsDelta = (int)((float) (scorePoints - oldest) / (float) 10);
        } else {
            scorePointsDelta = 0;
        }
    }
}
