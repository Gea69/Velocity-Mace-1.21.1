
package com.gea69.velocitymace;

import net.minecraft.world.entity.Entity;

/**
 * Data passed to MeteorImpactHandler.
 * Pending-hit state is owned by VelocityMaceAttackContext.
 */
public final class MeteorImpactAttackContext {

    private MeteorImpactAttackContext() {
    }

    public record Hit(
            Entity attacker,
            Entity target,
            double speed,
            float preDefenseDamage,
            int level
    ) {
    }
}
