
package com.gea69.velocitymace.mixin;

import com.gea69.velocitymace.MeteorImpactAttackContext;
import com.gea69.velocitymace.MeteorImpactHandler;
import com.gea69.velocitymace.VelocityMaceAttackContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MeteorImpactHitMixin {

    @Inject(method = "hurt", at = @At("RETURN"))
    private void velocityMace$triggerMeteorImpact(
            DamageSource damageSource,
            float amount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity target = (LivingEntity) (Object) this;

        if (target.level().isClientSide() || !cir.getReturnValue()) {
            return;
        }

        Entity attacker = damageSource.getEntity();

        if (attacker == null) {
            return;
        }

        VelocityMaceAttackContext.MeteorImpactHit hit =
                VelocityMaceAttackContext.consumeMeteorImpactHit(
                        target,
                        attacker
                );

        if (hit == null) {
            return;
        }

        MeteorImpactHandler.apply(
                new MeteorImpactAttackContext.Hit(
                        hit.attacker(),
                        hit.target(),
                        hit.speed(),
                        hit.preDefenseDamage(),
                        hit.level()
                )
        );
    }
}
