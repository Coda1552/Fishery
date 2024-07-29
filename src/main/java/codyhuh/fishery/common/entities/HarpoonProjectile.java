package codyhuh.fishery.common.entities;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class HarpoonProjectile extends Projectile {
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(HarpoonProjectile.class, EntityDataSerializers.INT);
    private HarpoonProjectile.HarpoonState currentState = HarpoonProjectile.HarpoonState.FLYING;
    public Entity hookedIn;

    public HarpoonProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public HarpoonProjectile(EntityType<? extends Projectile> type, Level level, Player owner) {
        this(type, level);
        setOwner(owner);
    }

    @Nullable
    public Player getPlayerOwner() {
        Entity entity = this.getOwner();
        return entity instanceof Player ? (Player)entity : null;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    private void setHookedEntity(@Nullable Entity p_150158_) {
        this.hookedIn = p_150158_;
        this.getEntityData().set(DATA_HOOKED_ENTITY, p_150158_ == null ? 0 : p_150158_.getId() + 1);
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_HOOKED_ENTITY, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_37153_) {
        if (DATA_HOOKED_ENTITY.equals(p_37153_)) {
            int i = this.getEntityData().get(DATA_HOOKED_ENTITY);
            this.hookedIn = i > 0 ? this.level().getEntity(i - 1) : null;
        }
    }

    @Nonnull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket(this, entity == null ? this.getId() : entity.getId());
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
    }

    public void handleEntityEvent(byte p_37123_) {
        if (p_37123_ == 31 && this.level().isClientSide && this.hookedIn instanceof Player && ((Player)this.hookedIn).isLocalPlayer()) {
            this.pullEntity(this.hookedIn);
        }

        super.handleEntityEvent(p_37123_);
    }

    protected void pullEntity(Entity p_150156_) {
        Entity entity = this.getOwner();
        if (entity != null) {
            Vec3 vec3 = (new Vec3(entity.getX() - this.getX(), entity.getY() - this.getY(), entity.getZ() - this.getZ())).scale(0.1D);
            p_150156_.setDeltaMovement(p_150156_.getDeltaMovement().add(vec3));
        }
    }

    public int retrieve() {
        Player player = (Player) this.getOwner();
        if (!this.level().isClientSide && player != null) {
            int i = 0;
            if (this.hookedIn != null) {
                this.pullEntity(this.hookedIn);
                this.level().broadcastEntityEvent(this, (byte)31);
                i = this.hookedIn instanceof ItemEntity ? 3 : 5;
            } /*else if (this.nibble > 0) {
                LootParams lootparams = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.TOOL, p_37157_).withParameter(LootContextParams.THIS_ENTITY, this).withParameter(LootContextParams.KILLER_ENTITY, this.getOwner()).withParameter(LootContextParams.THIS_ENTITY, this).withLuck((float)this.luck + player.getLuck()).create(LootContextParamSets.FISHING);
                LootTable loottable = this.level().getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
                List<ItemStack> list = loottable.getRandomItems(lootparams);
                event = new net.minecraftforge.event.entity.player.ItemFishedEvent(list, this.onGround() ? 2 : 1, this);
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
                if (event.isCanceled()) {
                    this.discard();
                    return event.getRodDamage();
                }
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)player, p_37157_, this, list);

                for(ItemStack itemstack : list) {
                    ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemstack);
                    double d0 = player.getX() - this.getX();
                    double d1 = player.getY() - this.getY();
                    double d2 = player.getZ() - this.getZ();
                    double d3 = 0.1D;
                    itementity.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                    this.level().addFreshEntity(itementity);
                    player.level().addFreshEntity(new ExperienceOrb(player.level(), player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, this.random.nextInt(6) + 1));
                    if (itemstack.is(ItemTags.FISHES)) {
                        player.awardStat(Stats.FISH_CAUGHT, 1);
                    }
                }

                i = 1;
            }*/

            if (this.onGround()) {
                i = 2;
            }

            return i;
        } else {
            return 0;
        }
    }

    public void tick() {
        super.tick();
        Player player = (Player) getOwner();
        if (player == null) {
            this.discard();
        }
        else if (this.level().isClientSide) {
            BlockPos blockpos = this.blockPosition();
            FluidState fluidstate = this.level().getFluidState(blockpos);

            if (this.currentState == HarpoonProjectile.HarpoonState.FLYING) {
                if (this.hookedIn != null) {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.currentState = HarpoonProjectile.HarpoonState.HOOKED_IN_ENTITY;
                    return;
                }

                this.checkCollision();
            } else {
                if (this.currentState == HarpoonProjectile.HarpoonState.HOOKED_IN_ENTITY) {
                    if (this.hookedIn != null) {
                        if (!this.hookedIn.isRemoved() && this.hookedIn.level().dimension() == this.level().dimension()) {
                            this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8D), this.hookedIn.getZ());
                        } else {
                            this.setHookedEntity(null);
                            this.currentState = HarpoonProjectile.HarpoonState.FLYING;
                        }
                    }

                    return;
                }
            }

            this.move(MoverType.SELF, this.getDeltaMovement());

            this.updateRotation();

            if (this.currentState == HarpoonProjectile.HarpoonState.FLYING && (this.onGround() || this.horizontalCollision)) {
                this.setDeltaMovement(Vec3.ZERO);
            }

            this.setDeltaMovement(this.getDeltaMovement());
            this.reapplyPosition();
        }
    }

    private void checkCollision() {
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() == HitResult.Type.MISS || !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) this.onHit(hitresult);
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        boolean isWater = entity instanceof LivingEntity living && living.getMobType() == MobType.WATER;

        entity.hurt(entity.damageSources().thrown(this, getOwner()), isWater ? 5.0F : 2.0F);

        if (!this.level().isClientSide) {
            this.setHookedEntity(entity);
        }
    }

    enum HarpoonState {
        FLYING,
        HOOKED_IN_ENTITY
    }
}
