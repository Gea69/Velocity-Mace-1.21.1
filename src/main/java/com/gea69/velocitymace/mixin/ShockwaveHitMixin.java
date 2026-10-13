
package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.ShockwaveHandler;
import com.gea69.velocitymace.VelocityMaceAttackContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ShockwaveHitMixin {

    @Inject(
            method = "hurt",
            at = @At("RETURN")
    )
    private void velocityMace$applyShockwaveAfterHit(
            DamageSource damageSource,
            float amount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity target = (LivingEntity) (Object) this;

        if (target.level().isClientSide()) {
            return;
        }

        Entity attacker = damageSource.getEntity();

        if (attacker == null) {
            return;
        }

        VelocityMaceAttackContext.ShockwaveHit hit =
                VelocityMaceAttackContext.consumeShockwaveHit(
                        target,
                        attacker
                );

        if (hit == null || !cir.getReturnValue()) {
            return;
        }

        ShockwaveHandler.apply(
                new com.gea69.velocitymace.ShockwaveAttackContext.Hit(
                        hit.attacker(),
                        hit.target(),
                        hit.damageSource(),
                        hit.preDefenseDamage(),
                        hit.enchantmentLevel()
                )
        );
    }
}
