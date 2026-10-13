
package com.gea69.velocitymace.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(FishingHook.class)
public abstract class LongCastFishingHookMixin {

    private static final double LONG_CAST_VELOCITY_BONUS_PER_LEVEL = 0.50D;
    private static final double LONG_CAST_RANGE_BONUS_PER_LEVEL = 1.25D;

    private static final double VANILLA_MAX_DISTANCE_SQUARED = 1024.0D;

    private static final ResourceKey<Enchantment> LONG_CAST_KEY =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(
                            "velocitymace",
                            "long_cast"
                    )
            );

    /**
     * Increases the bobber's initial casting velocity.
     *
     * Level I:   1.5x velocity
     * Level II:  2.0x velocity
     * Level III: 2.5x velocity
     * Level IV:  3.0x velocity
     */
    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V",
            at = @At("RETURN")
    )
    private void velocityMace$applyLongCast(
            Player player,
            Level level,
            int luck,
            int lureSpeed,
            CallbackInfo ci
    ) {
        if (level.isClientSide()) {
            return;
        }

        FishingHook hook = (FishingHook) (Object) this;

        int enchantmentLevel = Math.max(
                velocityMace$getLongCastLevel(
                        level.registryAccess(),
                        player.getMainHandItem()
                ),
                velocityMace$getLongCastLevel(
                        level.registryAccess(),
                        player.getOffhandItem()
                )
        );

        if (enchantmentLevel <= 0) {
            return;
        }

        double multiplier =
                1.0D + LONG_CAST_VELOCITY_BONUS_PER_LEVEL * enchantmentLevel;

        hook.setDeltaMovement(
                hook.getDeltaMovement().scale(multiplier)
        );
    }

    /**
     * Modifies the squared-distance limit used by shouldStopFishing.
     *
     * The vanilla limit is 1024.0D, equivalent to 32 blocks.
     * The threshold must scale by the square of the desired range multiplier.
     *
     * Level I:   2.25x range = 72 blocks
     * Level II:  3.50x range = 112 blocks
     * Level III: 4.75x range = 152 blocks
     * Level IV:  6.00x range = 192 blocks
     */
    @ModifyConstant(
            method = "shouldStopFishing(Lnet/minecraft/world/entity/player/Player;)Z",
            constant = @Constant(doubleValue = 1024.0D)
    )
    private double velocityMace$increaseMaximumDistance(
            double vanillaDistanceSquared
    ) {
        FishingHook hook = (FishingHook) (Object) this;
        Player owner = hook.getPlayerOwner();

        if (owner == null) {
            return vanillaDistanceSquared;
        }

        int enchantmentLevel = Math.max(
                velocityMace$getLongCastLevel(
                        hook.level().registryAccess(),
                        owner.getMainHandItem()
                ),
                velocityMace$getLongCastLevel(
                        hook.level().registryAccess(),
                        owner.getOffhandItem()
                )
        );

        if (enchantmentLevel <= 0) {
            return vanillaDistanceSquared;
        }

        double rangeMultiplier =
                1.0D + LONG_CAST_RANGE_BONUS_PER_LEVEL * enchantmentLevel;

        return vanillaDistanceSquared
                * rangeMultiplier
                * rangeMultiplier;
    }

    private static int velocityMace$getLongCastLevel(
            RegistryAccess registryAccess,
            ItemStack stack
    ) {
        Optional<Holder.Reference<Enchantment>> enchantment =
                registryAccess
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .get(LONG_CAST_KEY);

        return enchantment
                .map(holder ->
                        EnchantmentHelper.getItemEnchantmentLevel(
                                holder,
                                stack
                        )
                )
                .orElse(0);
    }
}
