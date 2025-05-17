package net.so_coretech.seismicexploration.entity.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class FollowPlayerGoal extends Goal {
    private final PathfinderMob mob;
    private Player target;
    private final double speed;
    private final float stopDistance;

    public FollowPlayerGoal(final PathfinderMob mob, final double speed, final float stopDistance) {
        this.mob = mob;
        this.speed = speed;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    public void setTarget(final Player player) {
        this.target = player;
    }

    @Override
    public boolean canUse() {
        return target != null && target.isAlive() && mob.distanceTo(target) > stopDistance;
    }

    @Override
    public void start() {
        // no op
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target != null) {
            mob.getNavigation().moveTo(target, speed);
        }
    }
}
