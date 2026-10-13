
package com.gea69.velocitymace.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class YoinkFishingHookMixin {

    private static final double YOINK_VERTICAL_FORCE = 0.42D;
    private static final double YOINK_HORIZONTAL_FORCE_PER_LEVEL = 0.5D;

    private static final ResourceKey<Enchantment> YOINK_KEY =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(
                            "velocitymace",
                            "yoink"
                    )
            );

    @Inject(
            method = "pullEntity(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("RETURN")
    )
    private void velocityMace$applyYoink(
            Entity hookedEntity,
            CallbackInfo ci
    ) {
        FishingHook hook = (FishingHook) (Object) this;

        if (!(hook.level() instanceof ServerLevel)) {
            return;
        }

        Player owner = hook.getPlayerOwner();
        if (owner == null || !owner.isAlive() || !hookedEntity.isAlive()) {
            return;
        }

        int yoinkLevel = Math.max(
                getYoinkLevel(hook, owner.getMainHandItem()),
                getYoinkLevel(hook, owner.getOffhandItem())
        );

        if (yoinkLevel <= 0) {
            return;
        }

        Vec3 towardOwner = owner.position()
                .subtract(hookedEntity.position());

        Vec3 horizontalDirection = new Vec3(
                towardOwner.x,
                0.0D,
                towardOwner.z
        );

        if (horizontalDirection.lengthSqr() > 1.0E-7D) {
            horizontalDirection = horizontalDirection.normalize();
        } else {
            horizontalDirection = Vec3.ZERO;
        }

        double horizontalForce =
                YOINK_HORIZONTAL_FORCE_PER_LEVEL * yoinkLevel;

        Vec3 impulse = new Vec3(
                horizontalDirection.x * horizontalForce,
                YOINK_VERTICAL_FORCE,
                horizontalDirection.z * horizontalForce
        );

        hookedEntity.setDeltaMovement(
                hookedEntity.getDeltaMovement().add(impulse)
        );

        hookedEntity.hurtMarked = true;
    }

    private static int getYoinkLevel(
            FishingHook hook,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return 0;
        }

        var enchantment = hook.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(YOINK_KEY);

        if (enchantment.isEmpty()) {
            return 0;
        }

        return EnchantmentHelper.getItemEnchantmentLevel(
                enchantment.get(),
                stack
        );
    }
}
