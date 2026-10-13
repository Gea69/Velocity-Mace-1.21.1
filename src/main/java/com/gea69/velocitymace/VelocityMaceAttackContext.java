
package com.gea69.velocitymace;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

public final class VelocityMaceAttackContext {

    private static final ThreadLocal<Player> ATTACKER = new ThreadLocal<>();
    private static final ThreadLocal<Entity> ATTACK_TARGET = new ThreadLocal<>();
    private static final ThreadLocal<Vec3> ATTACKER_VELOCITY = new ThreadLocal<>();
    private static final ThreadLocal<Vec3> TARGET_VELOCITY = new ThreadLocal<>();
    private static final ThreadLocal<ItemStack> WEAPON = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> FALL_FLYING = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> FLYING = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SPRINTING = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SWIMMING = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> CRAWLING = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> RIDING = new ThreadLocal<>();
    private static final ThreadLocal<Holder<Enchantment>> CURRENT_ENCHANTMENT =
            new ThreadLocal<>();

    private static final ThreadLocal<ShockwaveHit> SHOCKWAVE_HIT =
            new ThreadLocal<>();
    private static final ThreadLocal<MeteorImpactHit> METEOR_IMPACT_HIT =
            new ThreadLocal<>();

    private VelocityMaceAttackContext() {
    }

    public static void setAttack(
            Player attacker,
            Entity target,
            Vec3 attackerVelocity,
            Vec3 targetVelocity
    ) {
        ATTACKER.set(attacker);
        ATTACK_TARGET.set(target);
        ATTACKER_VELOCITY.set(attackerVelocity);
        TARGET_VELOCITY.set(targetVelocity);

        FALL_FLYING.set(attacker.isFallFlying());
        SPRINTING.set(attacker.isSprinting());
        SWIMMING.set(attacker.isSwimming());
        CRAWLING.set(attacker.isCrouching());
        RIDING.set(attacker.isPassenger());

        boolean flying = false;
        try {
            flying = attacker.getAbilities().flying;
        } catch (Exception ignored) {
            flying = false;
        }
        FLYING.set(flying);
    }

    public static Player getAttacker() {
        return ATTACKER.get();
    }

    public static Entity getTarget() {
        return ATTACK_TARGET.get();
    }

    public static Vec3 getAttackerVelocity() {
        return ATTACKER_VELOCITY.get();
    }

    public static Vec3 getTargetVelocity() {
        return TARGET_VELOCITY.get();
    }

    public static ItemStack getWeapon() {
        return WEAPON.get();
    }

    public static boolean isFallFlying() {
        Boolean value = FALL_FLYING.get();
        return value != null && value;
    }

    public static boolean isFlying() {
        Boolean value = FLYING.get();
        return value != null && value;
    }

    public static boolean isSprinting() {
        Boolean value = SPRINTING.get();
        return value != null && value;
    }

    public static boolean isSwimming() {
        Boolean value = SWIMMING.get();
        return value != null && value;
    }

    public static boolean isCrawling() {
        Boolean value = CRAWLING.get();
        return value != null && value;
    }

    public static boolean isRiding() {
        Boolean value = RIDING.get();
        return value != null && value;
    }

    public static void setWeapon(ItemStack weapon) {
        if (weapon == null || weapon.isEmpty()) {
            WEAPON.set(ItemStack.EMPTY);
        } else {
            WEAPON.set(weapon.copy());
        }
    }

    public static void setCurrentEnchantment(
            Holder<Enchantment> enchantment
    ) {
        CURRENT_ENCHANTMENT.set(enchantment);
    }

    public static Holder<Enchantment> getCurrentEnchantment() {
        return CURRENT_ENCHANTMENT.get();
    }

    public static void clearCurrentEnchantment() {
        CURRENT_ENCHANTMENT.remove();
    }

    // Shockwave pending-hit context

    public static void setShockwaveHit(
            Entity attacker,
            Entity target,
            DamageSource damageSource,
            float preDefenseDamage,
            int enchantmentLevel
    ) {
        SHOCKWAVE_HIT.set(new ShockwaveHit(
                attacker,
                target,
                damageSource,
                preDefenseDamage,
                enchantmentLevel
        ));
    }

    public static ShockwaveHit consumeShockwaveHit(
            Entity target,
            Entity attacker
    ) {
        ShockwaveHit hit = SHOCKWAVE_HIT.get();

        if (hit == null
                || hit.target() != target
                || hit.attacker() != attacker) {
            return null;
        }

        SHOCKWAVE_HIT.remove();
        return hit;
    }

    // Meteor Impact pending-hit context

    public static void setMeteorImpactHit(
            Entity attacker,
            Entity target,
            double speedBlocksPerSecond,
            float preDefenseDamage,
            int enchantmentLevel
    ) {
        METEOR_IMPACT_HIT.set(new MeteorImpactHit(
                attacker,
                target,
                speedBlocksPerSecond,
                preDefenseDamage,
                enchantmentLevel
        ));
    }

    public static MeteorImpactHit consumeMeteorImpactHit(
            Entity target,
            Entity attacker
    ) {
        MeteorImpactHit hit = METEOR_IMPACT_HIT.get();

        if (hit == null
                || hit.target() != target
                || hit.attacker() != attacker) {
            return null;
        }

        METEOR_IMPACT_HIT.remove();
        return hit;
    }

    /**
     * Clears the complete snapshot and any unconsumed effect hits.
     * Called when Player.attack finishes.
     */
    public static void clearTarget() {
        ATTACKER.remove();
        ATTACK_TARGET.remove();
        ATTACKER_VELOCITY.remove();
        TARGET_VELOCITY.remove();
        WEAPON.remove();

        FALL_FLYING.remove();
        FLYING.remove();
        SPRINTING.remove();
        SWIMMING.remove();
        CRAWLING.remove();
        RIDING.remove();

        CURRENT_ENCHANTMENT.remove();
        SHOCKWAVE_HIT.remove();
        METEOR_IMPACT_HIT.remove();
    }

    public record ShockwaveHit(
            Entity attacker,
            Entity target,
            DamageSource damageSource,
            float preDefenseDamage,
            int enchantmentLevel
    ) {
    }

    public record MeteorImpactHit(
            Entity attacker,
            Entity target,
            double speed,
            float preDefenseDamage,
            int level
    ) {
    }
}
