
package com.gea69.velocitymace;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

/**
 * Data passed to ShockwaveHandler.
 * Pending-hit state is owned by VelocityMaceAttackContext.
 */
public final class ShockwaveAttackContext {

    private ShockwaveAttackContext() {
    }

    public record Hit(
            Entity attacker,
            Entity target,
            DamageSource damageSource,
            float preDefenseDamage,
            int enchantmentLevel
    ) {
    }
}
