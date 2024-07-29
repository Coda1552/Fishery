package codyhuh.fishery.common.items;

import codyhuh.fishery.Fishery;
import codyhuh.fishery.common.entities.HarpoonProjectile;
import codyhuh.fishery.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

public class HarpoonLauncherItem extends Item {
    private boolean startSoundPlayed = false;
    private boolean midLoadSoundPlayed = false;

    public HarpoonLauncherItem(Properties builder) {
        super(builder);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gunStack = player.getItemInHand(hand);

        if (level instanceof ServerLevel sl) {

            var list = sl.getEntities(ModEntities.HARPOON.get(), e -> e.getOwner().getUUID().equals(player.getUUID()));

            for (HarpoonProjectile proj : list) {

                if (proj.hookedIn != null) {
                    proj.retrieve();
                }
            }

            if (isCharged(gunStack)) {
                performShooting(level, player, hand, gunStack, 1.0F);
                setCharged(gunStack, false);
                return InteractionResultHolder.consume(gunStack);
            }
            else {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
                player.startUsingItem(hand);

                return InteractionResultHolder.consume(gunStack);
            }
        }
        return InteractionResultHolder.fail(gunStack);
    }

    public void releaseUsing(ItemStack gunStack, Level level, LivingEntity holder, int ticksUsed) {
        int i = this.getUseDuration(gunStack) - ticksUsed;
        float f = getPowerForTime(i, gunStack);
        if (f >= 1.0F && !isCharged(gunStack) && tryCharge(holder, gunStack)) {
            setCharged(gunStack, true);
            SoundSource soundsource = holder instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            level.playSound(null, holder.getX(), holder.getY(), holder.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundsource, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        }

    }

    private static boolean tryCharge(LivingEntity holder, ItemStack stack) {
        int i = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.MULTISHOT, stack);
        int j = i == 0 ? 1 : 3;
        boolean flag = holder instanceof Player player && player.getAbilities().instabuild;
        ItemStack itemstack = holder.getProjectile(stack);
        ItemStack itemstack1 = itemstack.copy();

        for(int k = 0; k < j; ++k) {
            if (k > 0) {
                itemstack = itemstack1.copy();
            }

            if (itemstack.isEmpty() && flag) {
                itemstack = new ItemStack(Items.ARROW);
                itemstack1 = itemstack.copy();
            }

            if (!charge(stack)) {
                return false;
            }
        }

        return true;
    }

    private static boolean charge(ItemStack harpoon) {
        setCharged(harpoon, true);
        return true;
    }

    public static boolean isCharged(ItemStack stack) {
        CompoundTag compoundtag = stack.getTag();
        return compoundtag != null && compoundtag.getBoolean("Charged");
    }

    public static void setCharged(ItemStack stack, boolean charged) {
        CompoundTag compoundtag = stack.getOrCreateTag();
        compoundtag.putBoolean("Charged", charged);
    }

    private static void shootProjectile(Level level, LivingEntity holder, InteractionHand hand, ItemStack p_40898_, float p_40902_, float p_40903_, float p_40904_) {
        if (!level.isClientSide) {
            HarpoonProjectile projectile = new HarpoonProjectile(ModEntities.HARPOON.get(), level, (Player)holder);

            Vec3 vec31 = holder.getUpVector(1.0F);
            Quaternionf quaternionf = (new Quaternionf()).setAngleAxis((p_40904_ * ((float)Math.PI / 180F)), vec31.x, vec31.y, vec31.z);
            Vec3 vec3 = holder.getViewVector(1.0F);
            Vector3f vector3f = vec3.toVector3f().rotate(quaternionf);
            projectile.moveTo(holder.getX(), holder.getEyeY() + (double)0.1F, holder.getZ());
            projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), p_40902_, p_40903_);

            p_40898_.hurtAndBreak(1, holder, (p_40858_) -> {
                p_40858_.broadcastBreakEvent(hand);
            });
            level.addFreshEntity(projectile);
            level.playSound(null, holder.getX(), holder.getY(), holder.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void performShooting(Level level, LivingEntity holder, InteractionHand hand, ItemStack gunStack, float p_40893_) {
        if (holder instanceof Player player && ForgeEventFactory.onArrowLoose(gunStack, holder.level(), player, 1, true) < 0) return;
        float[] afloat = getShotPitches(holder.getRandom());

        shootProjectile(level, holder, hand, gunStack, afloat[0], p_40893_, 0.0F);

        if (holder instanceof ServerPlayer serverplayer) {
            serverplayer.awardStat(Stats.ITEM_USED.get(gunStack.getItem()));
        }
    }

    private static float[] getShotPitches(RandomSource p_220024_) {
        boolean flag = p_220024_.nextBoolean();
        return new float[]{1.0F, getRandomShotPitch(flag, p_220024_), getRandomShotPitch(!flag, p_220024_)};
    }

    private static float getRandomShotPitch(boolean p_220026_, RandomSource p_220027_) {
        float f = p_220026_ ? 0.63F : 0.43F;
        return 1.0F / (p_220027_.nextFloat() * 0.5F + 1.8F) + f;
    }

    public void onUseTick(Level p_40910_, LivingEntity p_40911_, ItemStack p_40912_, int p_40913_) {
        if (!p_40910_.isClientSide) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, p_40912_);
            SoundEvent soundevent = this.getStartSound(i);
            SoundEvent soundevent1 = i == 0 ? SoundEvents.CROSSBOW_LOADING_MIDDLE : null;
            float f = (float)(p_40912_.getUseDuration() - p_40913_) / (float)getChargeDuration(p_40912_);
            if (f < 0.2F) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }

            if (f >= 0.2F && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                p_40910_.playSound(null, p_40911_.getX(), p_40911_.getY(), p_40911_.getZ(), soundevent, SoundSource.PLAYERS, 0.5F, 1.0F);
            }

            if (f >= 0.5F && soundevent1 != null && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                p_40910_.playSound(null, p_40911_.getX(), p_40911_.getY(), p_40911_.getZ(), soundevent1, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }

    }

    public int getUseDuration(ItemStack p_40938_) {
        return getChargeDuration(p_40938_) + 3;
    }

    public static int getChargeDuration(ItemStack p_40940_) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, p_40940_);
        return i == 0 ? 25 : 25 - 5 * i;
    }

    public UseAnim getUseAnimation(ItemStack p_40935_) {
        return UseAnim.CROSSBOW;
    }

    private SoundEvent getStartSound(int p_40852_) {
        switch (p_40852_) {
            case 1:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_1;
            case 2:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_2;
            case 3:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_3;
            default:
                return SoundEvents.CROSSBOW_LOADING_START;
        }
    }

    private static float getPowerForTime(int p_40854_, ItemStack p_40855_) {
        float f = (float)p_40854_ / (float)getChargeDuration(p_40855_);
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public void appendHoverText(ItemStack gunStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        if (isCharged(gunStack)) {
            components.add(Component.translatable("item." + Fishery.MOD_ID + ".harpoon_launcher.loaded").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }

    public boolean useOnRelease(ItemStack stack) {
        return stack.is(this);
    }
}
