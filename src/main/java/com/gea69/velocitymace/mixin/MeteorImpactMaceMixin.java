
package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.MeteorImpact;
import com.gea69.velocitymace.VelocityMaceAttackContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MaceItem.class)
public abstract class MeteorImpactMaceMixin {

    @Inject(
            method = "getAttackDamageBonus",
            at = @At("RETURN")
    )
    private void velocityMace$captureMeteorImpact(
            Entity target,
            float damage,
            DamageSource damageSource,
            CallbackInfoReturnable<Float> cir
    ) {
        if (target == null || target.level().isClientSide()) {
            return;
        }

        Entity source = damageSource.getEntity();

        if (!(source instanceof LivingEntity attacker)) {
            return;
        }

        ItemStack weapon = damageSource.getWeaponItem();
        int level = MeteorImpact.getLevel(weapon);

        if (level <= 0 || !MaceItem.canSmashAttack(attacker)) {
            return;
        }

        float preDefenseDamage = damage + cir.getReturnValue();

        if (preDefenseDamage <= 0.0F
                || !Float.isFinite(preDefenseDamage)) {
            return;
        }

        Vec3 relativeVelocity = attacker.getDeltaMovement()
                .subtract(target.getDeltaMovement());

        double speed = relativeVelocity.length() * 20.0D;

        if (!Double.isFinite(speed) || speed <= 0.0D) {
            return;
        }

        VelocityMaceAttackContext.setMeteorImpactHit(
                attacker,
                target,
                speed,
                preDefenseDamage,
                level
        );
    }
}
