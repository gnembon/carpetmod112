package carpet.helpers.lifetime;

import carpet.helpers.lifetime.filter.EntityFilterManager;
import carpet.helpers.lifetime.removal.RemovalReason;
import carpet.helpers.lifetime.spawning.SpawningReason;
import carpet.helpers.lifetime.trackeddata.BasicTrackedData;
import carpet.helpers.lifetime.trackeddata.ExperienceOrbTrackedData;
import carpet.helpers.lifetime.trackeddata.ItemTrackedData;
import carpet.helpers.lifetime.utils.LifeTimeTrackerUtil;
import carpet.helpers.lifetime.utils.SpecificDetailMode;
import carpet.helpers.lifetime.utils.TextUtil;
import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.WorldServer;

import java.util.*;

public class LifeTimeWorldTracker
{
    private final WorldServer world;
    private final Map<Class<? extends Entity>, BasicTrackedData> dataMap = Maps.newHashMap();
    // a counter which accumulates when spawning stage occurs
    // it's used to determine life time
    private long spawnStageCounter;

    public LifeTimeWorldTracker(WorldServer world)
    {
        this.world = world;
    }

    public Map<Class<? extends Entity>, BasicTrackedData> getDataMap()
    {
        return this.dataMap;
    }

    public void initTracker()
    {
        this.dataMap.clear();
    }

    private Optional<BasicTrackedData> getTrackedData(Entity entity)
    {
        if (LifeTimeTracker.getInstance().willTrackEntity(entity))
        {
            return Optional.of(this.dataMap.computeIfAbsent(entity.getClass(), (e -> {
                if (entity instanceof EntityItem)
                {
                    return new ItemTrackedData();
                }
                if (entity instanceof EntityXPOrb)
                {
                    return new ExperienceOrbTrackedData();
                }
                return new BasicTrackedData();
            })));
        }
        return Optional.empty();
    }

    public void onEntitySpawn(Entity entity, SpawningReason reason)
    {
        this.getTrackedData(entity).ifPresent(data -> data.updateSpawning(entity, reason));
    }

    public void onEntityRemove(Entity entity, RemovalReason reason)
    {
        this.getTrackedData(entity).ifPresent(data -> data.updateRemoval(entity, reason));
    }

    public void increaseSpawnStageCounter()
    {
        this.spawnStageCounter++;
    }

    public long getSpawnStageCounter()
    {
        return this.spawnStageCounter;
    }

    private List<ITextComponent> addIfEmpty(List<ITextComponent> list, ITextComponent text)
    {
        if (list.isEmpty())
        {
            list.add(text);
        }
        return list;
    }

    protected int print(ICommandSender source, long ticks, Class<? extends Entity> specificType, SpecificDetailMode detailMode)
    {
        // existence check
        BasicTrackedData specificData = this.dataMap.get(specificType);
        if (this.dataMap.isEmpty() || (specificType != null && specificData == null))
        {
            return 0;
        }

        // dimension name header
        // Overworld (overworld)
        List<ITextComponent> result = Lists.newArrayList();
        result.add(Messenger.s(null, " "));
        ITextComponent dimText = TextUtil.getDimensionNameText(this.world.provider.getDimensionType());
        dimText.getStyle().setColor(TextFormatting.GOLD).setBold(true);
        result.add(Messenger.c(
                dimText,
                String.format("g  (%s)", this.world.provider.getDimensionType().getName())
        ));

        if (specificType == null)
        {
            this.printAll(ticks, result);
        }
        else
        {
            this.printSpecific(ticks, specificType, specificData, detailMode, result);
        }
        Messenger.send(source, result);
        return 1;
    }

    private void printAll(long ticks, List<ITextComponent> result)
    {
        // sorted by spawn count
        // will being sorting by avg life time better?
        this.dataMap.entrySet().stream().
                sorted(Collections.reverseOrder(Comparator.comparingLong(a -> a.getValue().getSpawningCount()))).
                forEach((entry) -> {
                    Class<? extends Entity> entityType = entry.getKey();
                    BasicTrackedData data = entry.getValue();
                    List<ITextComponent> spawningReasons = data.getSpawningReasonsTexts(ticks, true);
                    List<ITextComponent> removalReasons = data.getRemovalReasonsTexts(ticks, true);
                    String currentCommandBase = String.format("/%s %s", LifeTimeTracker.getInstance().getCommandPrefix(), LifeTimeTrackerUtil.getEntityTypeDescriptor(entityType));
                    // [Creeper] S/R: 21/8, L: 145/145/145.00 (gt)
                    result.add(Messenger.c(
                            "g - [",
                            TextUtil.getFancyText(
                                    null,
                                    Messenger.s(null, LifeTimeTrackerUtil.getEntityTypeDescriptor(entityType)),
                                    Messenger.c(
                                            "w Filter: ",
                                            EntityFilterManager.getInstance().getEntityFilterText(entityType),
                                            "g  / [G] ",
                                            EntityFilterManager.getInstance().getEntityFilterText(null),
                                            "w \nClick to show detail"
                                    ),
                                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentCommandBase)
                            ),
                            "g ] ",
                            TextUtil.getFancyText(
                                    null,
                                    Messenger.c("e S", "g /", "r R"),
                                    Messenger.c("e Spawn Count", "g  / ", "r Removal Count"),
                                    null
                            ),
                            "g : ",
                            TextUtil.getFancyText(
                                    null,
                                    Messenger.c("e " + data.getSpawningCount()),
                                    Messenger.s(null,
                                            Messenger.c(
                                                    data.getSpawningCountText(ticks),
                                                    "w " + (spawningReasons.isEmpty() ? "" : "\n"),
                                                    Messenger.c(spawningReasons.toArray(new Object[0]))
                                            ).getUnformattedText()  // to reduce network load
                                    ),
                                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("%s %s", currentCommandBase, SpecificDetailMode.SPAWNING))
                            ),
                            "g /",
                            TextUtil.getFancyText(
                                    null,
                                    Messenger.c("r " + data.getRemovalCount()),
                                    Messenger.s(null,
                                            Messenger.c(
                                                    data.getRemovalCountText(ticks),
                                                    "w " + (removalReasons.isEmpty() ? "" : "\n"),
                                                    Messenger.c(removalReasons.toArray(new Object[0]))
                                            ).getUnformattedText()  // to reduce network load
                                    ),
                                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("%s %s", currentCommandBase, SpecificDetailMode.REMOVAL))
                            ),
                            "g , ",
                            TextUtil.getFancyText(
                                    null,
                                    Messenger.c(
                                            "q L", "g : ",
                                            data.lifeTimeStatistic.getCompressedResult(true)
                                    ),
                                    Messenger.s(null,
                                            Messenger.c(
                                                    "q Life Time Overview\n",
                                                    data.lifeTimeStatistic.getResult("", true)
                                            ).getUnformattedText()  // to reduce network load
                                    ),
                                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("%s %s", currentCommandBase, SpecificDetailMode.LIFE_TIME))
                            )
                    ));
                });
    }

    private void printSpecific(long ticks, Class<? extends Entity> specificType, BasicTrackedData specificData, SpecificDetailMode detailMode, List<ITextComponent> result)
    {
        result.add(Messenger.c(
                "c Filter: ",
                EntityFilterManager.getInstance().getEntityFilterText(specificType),
                "g  / ",
                TextUtil.getFancyText("g", Messenger.s(null, "[G]"), Messenger.s(null, "Global"), null),
                "g  ",
                EntityFilterManager.getInstance().getEntityFilterText(null)
        ));
        boolean showLifeTime = detailMode == null || detailMode == SpecificDetailMode.LIFE_TIME;
        boolean showSpawning = detailMode == null || detailMode == SpecificDetailMode.SPAWNING;
        boolean showRemoval = detailMode == null || detailMode == SpecificDetailMode.REMOVAL;
        if (showSpawning)
        {
            result.add(specificData.getSpawningCountText(ticks));
        }
        if (showRemoval)
        {
            result.add(specificData.getRemovalCountText(ticks));
        }
        if (showLifeTime)
        {
            result.add(TextUtil.getFancyText(
                    "q",
                    Messenger.s(null, "Life Time Overview"),
                    Messenger.s(null, "The amount of spawning stage passing between entity spawning and entity removal"),
                    null
            ));
            result.add(specificData.lifeTimeStatistic.getResult("", false));
        }
        if (showSpawning)
        {
            result.add(Messenger.s(null, "Reasons for spawning", "e"));
            result.addAll(this.addIfEmpty(specificData.getSpawningReasonsTexts(ticks, false), Messenger.s(null, "  N/A", "g")));
        }
        if (showRemoval)
        {
            result.add(Messenger.s(null, "Reasons for removal", "r"));
            result.addAll(this.addIfEmpty(specificData.getRemovalReasonsTexts(ticks, false), Messenger.s(null, "  N/A", "g")));
        }
    }
}
