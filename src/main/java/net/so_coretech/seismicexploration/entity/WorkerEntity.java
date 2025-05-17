package net.so_coretech.seismicexploration.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.so_coretech.seismicexploration.ModNetworking;
import net.so_coretech.seismicexploration.entity.ai.goal.DeploySensorsGoal;
import net.so_coretech.seismicexploration.entity.ai.goal.FollowPlayerGoal;
import net.so_coretech.seismicexploration.network.OpenWorkerOrderMenuPacket;

import javax.annotation.Nullable;

public class WorkerEntity extends PathfinderMob {

    private final String name;
    private final ItemStackHandler inventory = new ItemStackHandler(9);
    private final LazyOptional<ItemStackHandler> inventoryCap = LazyOptional.of(() -> inventory);
    private boolean frozen;

    public WorkerEntity(final EntityType<? extends PathfinderMob> type, final Level level) {
        super(type, level);
        this.name = "Bob";
    }

    public void setFrozen(final boolean frozen) {
        this.frozen = frozen;
    }

    /**
     * Orders the NPC to follow the player, removing all goals and adding a follow player goal.
     *
     * @param player The player who interacted with the NPC.
     */
    public void setFollowTarget(final Player player) {
        this.goalSelector.removeAllGoals(goal -> true);
        final FollowPlayerGoal goal = new FollowPlayerGoal(this, 1.2D, 2.0F);
        goal.setTarget(player);
        this.goalSelector.addGoal(1, goal);

        player.displayClientMessage(Component.literal(String.format("<%s> Alright, I'll follow you.", name)), false);
    }

    /**
     * Orders the NPC to deploy sensors, removing all goals and adding a move out and return goal.
     *
     * @param player    The player who interacted with the NPC.
     * @param startPos  The starting position for the deployment.
     * @param direction The direction to deploy the sensors.
     * @param count     The number of sensors to deploy.
     * @param gap       The gap between each sensor.
     */
    public void setDeploySensors(final Player player, final BlockPos startPos, final Direction direction,
                                 final int count, final int gap) {
        this.goalSelector.removeAllGoals(goal -> true);

        // TODO: use a goal where the NPC drops sensors
        final DeploySensorsGoal goal = new DeploySensorsGoal(this, startPos, direction, count, gap, (reason) -> {
            player.displayClientMessage(Component.literal(String.format("<%s> I completed my task!", name)), false);
            setFree(player);
        });
        this.goalSelector.addGoal(1, goal);

        player.displayClientMessage(Component.literal(String.format("<%s> Understood, I'll start right away!.", name)), false);
    }

    /**
     * Orders the NPC to free roam mode, removing all goals and adding a random stroll goal.
     *
     * @param player The player who interacted with the NPC.
     */
    public void setFree(final Player player) {
        this.goalSelector.removeAllGoals(goal -> true);
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0D));

        player.displayClientMessage(Component.literal(String.format("<%s> See you later!", name)), false);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, @Nullable final Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Inventory")) {
            final var provider = this.level().registryAccess();
            inventory.deserializeNBT(provider, tag.getCompoundOrEmpty("Inventory"));
        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        final var provider = this.level().registryAccess();
        tag.put("Inventory", inventory.serializeNBT(provider));
    }

    @Override
    public void aiStep() {
        if (!frozen) {
            super.aiStep();
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0D));
    }

    @Override
    public InteractionResult interactAt(final Player player, final Vec3 vec, final InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            // Make the NPC face the player
            this.lookAt(player, 360.0F, 360.0F);
            // Sync body rotation to head rotation for instant full turn
            this.yBodyRot = this.getYHeadRot();
            this.setYRot(this.getYHeadRot());
            // Make the NPC stop moving
            setFrozen(true);
            // Open the worker order menu
            ModNetworking.sendToPlayer((ServerPlayer) player,
                new OpenWorkerOrderMenuPacket(this.getId(), player.blockPosition()));
            return InteractionResult.SUCCESS;
        }
        return super.interactAt(player, vec, hand);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                            .add(Attributes.MAX_HEALTH, 20.0D)
                            .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }
}

