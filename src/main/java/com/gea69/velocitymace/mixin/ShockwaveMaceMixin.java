
package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.Shockwave;
import com.gea69.velocitymace.VelocityMaceAttackContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MaceItem.class)
public abstract class ShockwaveMaceMixin {

    @Inject(
            method = "getAttackDamageBonus",
            at = @At("RETURN")
    )
    private void velocityMace$captureShockwaveDamage(
            Entity target,
            float damage,
            DamageSource damageSource,
            CallbackInfoReturnable<Float> cir
    ) {
        if (target.level().isClientSide()) {
            return;
        }

        Entity attacker = damageSource.getEntity();

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        ItemStack weapon = damageSource.getWeaponItem();
        int level = Shockwave.getLevel(weapon);

        if (level <= 0) {
            return;
        }

        if (!MaceItem.canSmashAttack(livingAttacker)) {
            return;
        }

        float preDefenseDamage = damage + cir.getReturnValue();

        if (preDefenseDamage <= 0.0F
                || !Float.isFinite(preDefenseDamage)) {
            return;
        }

        VelocityMaceAttackContext.setShockwaveHit(
                attacker,
                target,
                damageSource,
                preDefenseDamage,
                level
        );
    }
}
