package carpet.helpers;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.mixin.accessors.GoalSelectorAccessor;
import carpet.mixin.accessors.MobEntityAccessor;
import carpet.utils.extensions.AccessibleGoalSelectorEntry;
import net.minecraft.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.goal.AttackWithOwnerGoal;
import net.minecraft.entity.ai.goal.AvoidSunlightGoal;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.entity.ai.goal.CreeperIgniteGoal;
import net.minecraft.entity.ai.goal.DoorInteractGoal;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.EscapeSunlightGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FlyOntoTreeGoal;
import net.minecraft.entity.ai.goal.FollowMobGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.FollowTargetIfTamedGoal;
import net.minecraft.entity.ai.goal.FormCaravanGoal;
import net.minecraft.entity.ai.goal.GoToEntityTargetGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.HorseBondWithPlayerGoal;
import net.minecraft.entity.ai.goal.IronGolemLookGoal;
import net.minecraft.entity.ai.goal.LongDoorInteractGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SitOnOwnerShoulderGoal;
import net.minecraft.entity.ai.goal.SkeletonHorseTrapTriggerGoal;
import net.minecraft.entity.ai.goal.StopFollowingCustomerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.TrackIronGolemTargetGoal;
import net.minecraft.entity.ai.goal.TrackOwnerAttackerGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.goal.WolfBegGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.CommonI18n;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AIHelper {
    private static final WeakHashMap<GoalSelector, MobEntity> TASK_TO_ENTITY_MAP = new WeakHashMap<>();
    private static final WeakHashMap<MobEntity, WeakHashMap<Goal, Supplier<String>>> DETAILED_INFO = new WeakHashMap<>();
    private static final Map<Class<? extends Goal>, String> TASK_NAME_MAP = new HashMap<>();

    static {
        TASK_NAME_MAP.put(MeleeAttackGoal.class, "Melee attack");
        TASK_NAME_MAP.put(ProjectileAttackGoal.class, "Ranged attack");
        TASK_NAME_MAP.put(BowAttackGoal.class, "Ranged bow attack");
        TASK_NAME_MAP.put(FleeEntityGoal.class, "Avoiding other entity");
        TASK_NAME_MAP.put(WolfBegGoal.class, "Beg");
        TASK_NAME_MAP.put(BreakDoorGoal.class, "Breaking door");
        TASK_NAME_MAP.put(CreeperIgniteGoal.class, "Creeper swelling");
        TASK_NAME_MAP.put(TrackIronGolemTargetGoal.class, "Defending village");
        TASK_NAME_MAP.put(DoorInteractGoal.class, "Interacting with door");
        TASK_NAME_MAP.put(EatGrassGoal.class, "Eating grass");
        TASK_NAME_MAP.put(class_6502.class, "Looking for nearest other entity");
        TASK_NAME_MAP.put(class_3092.class, "Looking for nearest player");
        TASK_NAME_MAP.put(EscapeSunlightGoal.class, "Seeking shelter from the sun");
        TASK_NAME_MAP.put(FollowMobGoal.class, "Following other entity");
        TASK_NAME_MAP.put(class_3219.class, "Following golem");
        TASK_NAME_MAP.put(FollowOwnerGoal.class, "Following owner");
        TASK_NAME_MAP.put(class_2744.class, "Following owner while flying");
        TASK_NAME_MAP.put(FollowParentGoal.class, "Following parent");
        TASK_NAME_MAP.put(class_6476.class, "Farming");
        TASK_NAME_MAP.put(RevengeGoal.class, "Hurt by another entity");
        TASK_NAME_MAP.put(SitOnOwnerShoulderGoal.class, "Land on owners sholder");
        TASK_NAME_MAP.put(PounceAtTargetGoal.class, "Leaping at target");
        TASK_NAME_MAP.put(FormCaravanGoal.class, "Llama following caravan");
        TASK_NAME_MAP.put(LookAtCustomerGoal.class, "Looking at player");
        TASK_NAME_MAP.put(IronGolemLookGoal.class, "Looking at villager");
        TASK_NAME_MAP.put(LookAroundGoal.class, "Idle, looking around");
        TASK_NAME_MAP.put(AnimalMateGoal.class, "Mating (Animals)");
        TASK_NAME_MAP.put(class_6485.class, "Moving indoors");
        TASK_NAME_MAP.put(MoveThroughVillageGoal.class, "Moving through village");
        TASK_NAME_MAP.put(MoveToTargetPosGoal.class, "Moving to block");
        TASK_NAME_MAP.put(GoToWalkTargetGoal.class, "Moving towards restriction");
        TASK_NAME_MAP.put(GoToEntityTargetGoal.class, "Moving towards target");
        TASK_NAME_MAP.put(FollowTargetGoal.class, "Looking for nearest target");
        TASK_NAME_MAP.put(AttackGoal.class, "Ocelot attacking");
        TASK_NAME_MAP.put(CatSitOnBlockGoal.class, "Ocelot sitting");
        TASK_NAME_MAP.put(LongDoorInteractGoal.class, "Opening door");
        TASK_NAME_MAP.put(TrackOwnerAttackerGoal.class, "Owner hurt by target");
        TASK_NAME_MAP.put(AttackWithOwnerGoal.class, "Owner hurts target");
        TASK_NAME_MAP.put(EscapeDangerGoal.class, "Panicking");
        TASK_NAME_MAP.put(class_6488.class, "Playing");
        TASK_NAME_MAP.put(class_6491.class, "Prevented from opening door");
        TASK_NAME_MAP.put(AvoidSunlightGoal.class, "Avoiding sun");
        TASK_NAME_MAP.put(HorseBondWithPlayerGoal.class, "Running around like crazy");
        TASK_NAME_MAP.put(SitGoal.class, "Sitting");
        TASK_NAME_MAP.put(SkeletonHorseTrapTriggerGoal.class, "Riding");
        TASK_NAME_MAP.put(SwimGoal.class, "Swimming");
        TASK_NAME_MAP.put(TrackTargetGoal.class, "Targeting");
        TASK_NAME_MAP.put(FollowTargetIfTamedGoal.class, "Targeting untamed animal");
        TASK_NAME_MAP.put(TemptGoal.class, "Tempted by player");
        TASK_NAME_MAP.put(StopFollowingCustomerGoal.class, "Trading with player");
        TASK_NAME_MAP.put(class_6496.class, "Interacting with villager");
        TASK_NAME_MAP.put(class_6483.class, "Mating (Villagers)");
        TASK_NAME_MAP.put(WanderAroundGoal.class, "Wandering");
        TASK_NAME_MAP.put(WanderAroundFarGoal.class, "Wandering (Land)");
        TASK_NAME_MAP.put(FlyOntoTreeGoal.class, "Wandering (Air)");
        TASK_NAME_MAP.put(LookAtEntityGoal.class, "Looking at closest entity");
        TASK_NAME_MAP.put(ZombieAttackGoal.class, "Zombie attacking");
    }

    public static Stream<Goal> getCurrentTasks(MobEntity e) {
        return ((GoalSelectorAccessor) getTasks(e)).getExecutingTaskEntries().stream()
                .sorted(Comparator.comparingInt(AccessibleGoalSelectorEntry::getPriority).reversed())
                .map(AccessibleGoalSelectorEntry::getAction);
    }

    public static Stream<String> getCurrentTaskNames(MobEntity e, Map<Goal, Supplier<String>> details) {
        return getCurrentTasks(e).map(task -> getTaskName(task, details));
    }

    public static String getTaskName(Goal task) {
        return getTaskName(task, null);
    }

    public static String getTaskName(Goal task, Map<Goal, Supplier<String>> details) {
        String detailsInfo = details != null && details.containsKey(task) ? ": " + details.get(task).get() : "";
        String taskName = "Unknown";
        for (Class<? extends Goal> cls = task.getClass(); cls != Goal.class; cls = (Class<? extends Goal>) cls.getSuperclass()) {
            String name = TASK_NAME_MAP.get(cls);
            if (name != null) {
                taskName = name;
                break;
            }
        }
        return taskName + detailsInfo;
    }

    public static String getInfo(GoalSelector tasks, Goal task) {
        return "Entity: " + getName(getOwner(tasks)) + ", Task: " + getTaskName(task);
    }

    public static String getName(@Nullable Entity entity) {
        if (entity == null) return "unknown";
        if (!entity.method_34200()) return entity.method_29611();
        String id = class_2245.method_34602(entity);
        if (id == null) id = "generic";
        return CommonI18n.translate("entity." + id + ".name");
    }

    public static Optional<String> formatCurrentTasks(MobEntity e, Map<Goal, Supplier<String>> details) {
        return Optional.of(getCurrentTaskNames(e, details).collect(Collectors.joining(",")));
    }

    @Nullable
    public static MobEntity getOwner(GoalSelector tasks) {
        return TASK_TO_ENTITY_MAP.computeIfAbsent(tasks, t -> {
            for (ServerWorld world : CarpetServer.minecraft_server.worlds) {
                for (MobEntity e : world.method_26034(MobEntity.class, x -> true)) {
                    if (((MobEntityAccessor) e).getGoalSelector() == tasks) return e;
                }
            }
            return null;
        });
    }

    public static void update(GoalSelector tasks) {
        if (!CarpetSettings.displayMobAI) return;
        MobEntity owner = getOwner(tasks);
        if (owner == null) return;
        Map<Goal, Supplier<String>> details = DETAILED_INFO.get(owner);
        Optional<String> formatted = formatCurrentTasks(owner, details);
        if (!formatted.isPresent()) return;
        owner.method_34525(formatted.get());
    }
    public static void setDetailedInfo(MobEntity owner, Goal task, String info) {
        setDetailedInfo(owner, task, () -> info);
    }

    public static void setDetailedInfo(MobEntity owner, Goal task, Supplier<String> info) {
        DETAILED_INFO.computeIfAbsent(owner, x -> new WeakHashMap<>()).put(task, info);
        TASK_TO_ENTITY_MAP.put(getTasks(owner), owner);
        update(getTasks(owner));
    }

    public static GoalSelector getTasks(MobEntity e) {
        return ((MobEntityAccessor) e).getGoalSelector();
    }
}
