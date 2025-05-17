package net.so_coretech.seismicexploration.entity.ai.goal;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class MoveOutAndReturnGoal extends Goal {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final PathfinderMob mob;
    private final BlockPos startPos;
    private final Direction direction;
    private final int distance;
    private final GoalFinishedListener listener;

    private enum Phase {TO_START, OUT, RETURN, DONE}

    private Phase phase = Phase.TO_START;
    private @Nullable BlockPos outTarget = null;

    public MoveOutAndReturnGoal(final PathfinderMob mob, final BlockPos startPos, final Direction direction,
                                final int distance, final GoalFinishedListener listener) {
        this.mob = mob;
        this.startPos = getHighestBlock(this.mob.level(), startPos);
        this.direction = direction;
        this.distance = distance;
        this.listener = listener;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Always allow starting if not already done
        return phase != Phase.DONE;
    }

    @Override
    public void start() {
        phase = Phase.TO_START;
    }

    @Override
    public void tick() {
        switch (phase) {
            case TO_START -> {
                if (!mob.blockPosition().closerThan(startPos, 1.2)) {
                    LOGGER.trace("At {}, moving to start position: {} ({})",
                        mob.blockPosition(), startPos, mob.blockPosition().distSqr(startPos));
                    final BlockPos target = getHighestBlock(this.mob.level(), startPos);
                    mob.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
                } else {
                    LOGGER.trace("Arrived at start position. Starting to move out.");
                    phase = Phase.OUT;
                    outTarget = getHighestBlock(this.mob.level(),
                        mob.blockPosition().relative(direction, distance));
                }
            }
            case OUT -> {
                if (outTarget == null) {
                    outTarget = getHighestBlock(this.mob.level(),
                        mob.blockPosition().relative(direction, distance));
                }
                if (!mob.blockPosition().closerThan(outTarget, 1.2)) {
                    LOGGER.trace("At {}, moving out to target position: {} ({})",
                        mob.blockPosition(), outTarget, mob.blockPosition().distSqr(outTarget));
                    mob.getNavigation().moveTo(outTarget.getX() + 0.5, outTarget.getY(), outTarget.getZ() + 0.5, 1.0);
                } else {
                    LOGGER.trace("Arrived at target position. Starting to return.");
                    phase = Phase.RETURN;
                    outTarget = null;
                }
            }
            case RETURN -> {
                if (!mob.blockPosition().closerThan(startPos, 1.2)) {
                    LOGGER.trace("At {}, returning to start position: {}({})",
                        mob.blockPosition(), startPos, mob.blockPosition().distSqr(startPos));
                    final BlockPos target = getHighestBlock(this.mob.level(), startPos);
                    mob.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
                } else {
                    LOGGER.trace("Arrived at start position. Task complete.");
                    phase = Phase.DONE;
                    listener.onGoalFinished();
                }
            }
            case DONE -> mob.getNavigation().stop();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return phase != Phase.DONE;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    private static BlockPos getHighestBlock(final Level level, final BlockPos pos) {
        // Returns the topmost solid block at (x, z)
        return level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);
    }

    public interface GoalFinishedListener {
        void onGoalFinished();
    }
}
