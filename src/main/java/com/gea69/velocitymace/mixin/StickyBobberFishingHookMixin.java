
package com.gea69.velocitymace.mixin;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class StickyBobberFishingHookMixin {

    @Unique
    private static final ResourceKey<Enchantment> VELOCITYMACE$STICKY_BOBBER_KEY =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(
                            "velocitymace",
                            "sticky_bobber"
                    )
            );

    @Unique
    private boolean velocityMace$stuck;

    @Unique
    private BlockPos velocityMace$stuckBlockPos;

    @Unique
    private Direction velocityMace$stuckFace;

    @Unique
    private Vec3 velocityMace$hitLocation;

    @Unique
    private Vec3 velocityMace$anchorPosition;

    /**
     * Record the collision result before vanilla processes the hit.
     * The BlockHitResult is authoritative; no later face-geometry
     * rediscovery is required.
     */
    @Inject(
            method = "onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V",
            at = @At("HEAD")
    )
    private void velocityMace$recordStickyCollision(
            BlockHitResult hitResult,
            CallbackInfo ci
    ) {
        FishingHook hook = (FishingHook) (Object) this;

        if (velocityMace$getStickyBobberLevel(hook) <= 0) {
            velocityMace$clearStickyState();
            return;
        }

        velocityMace$stuck = true;
        velocityMace$stuckBlockPos = hitResult.getBlockPos().immutable();
        velocityMace$stuckFace = hitResult.getDirection();
        velocityMace$hitLocation = hitResult.getLocation();

        // Keep the entity at its current position. The hit location is
        // recorded separately because it is the collision point, not
        // necessarily the entity's position.
        velocityMace$anchorPosition = hook.position();

        // Stop immediately, before vanilla collision response runs.
        hook.setDeltaMovement(Vec3.ZERO);
    }

    /**
     * Vanilla may alter the bobber's velocity while processing the hit.
     * Zero it again after that processing completes.
     */
    @Inject(
            method = "onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V",
            at = @At("TAIL")
    )
    private void velocityMace$stopAfterCollision(
            BlockHitResult hitResult,
            CallbackInfo ci
    ) {
        if (!velocityMace$stuck) {
            return;
        }

        FishingHook hook = (FishingHook) (Object) this;

        if (velocityMace$getStickyBobberLevel(hook) <= 0) {
            velocityMace$clearStickyState();
            return;
        }

        hook.setDeltaMovement(Vec3.ZERO);
    }

    /**
     * Prevent gravity and other velocity changes earlier in tick()
     * from producing movement while the bobber is stuck.
     */
    @ModifyArg(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/world/entity/Entity;move(" +
                                    "Lnet/minecraft/world/entity/MoverType;" +
                                    "Lnet/minecraft/world/phys/Vec3;)V"
            ),
            index = 1
    )
    private Vec3 velocityMace$cancelStuckMovement(Vec3 movement) {
        if (!velocityMace$stuck) {
            return movement;
        }

        FishingHook hook = (FishingHook) (Object) this;

        if (velocityMace$getStickyBobberLevel(hook) <= 0
                || !velocityMace$isRecordedBlockValid(hook)) {
            velocityMace$clearStickyState();
            return movement;
        }

        hook.setDeltaMovement(Vec3.ZERO);
        return Vec3.ZERO;
    }

    /**
     * Maintain the anchor after vanilla's tick logic has completed.
     */
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void velocityMace$maintainStickyAnchor(CallbackInfo ci) {
        if (!velocityMace$stuck) {
            return;
        }

        FishingHook hook = (FishingHook) (Object) this;

        if (velocityMace$getStickyBobberLevel(hook) <= 0
                || !velocityMace$isRecordedBlockValid(hook)) {
            velocityMace$clearStickyState();
            return;
        }

        if (velocityMace$anchorPosition != null) {
            hook.setPos(
                    velocityMace$anchorPosition.x,
                    velocityMace$anchorPosition.y,
                    velocityMace$anchorPosition.z
            );
        }

        hook.setDeltaMovement(Vec3.ZERO);
    }

    @Unique
    private int velocityMace$getStickyBobberLevel(FishingHook hook) {
        var owner = hook.getOwner();

        if (!(owner instanceof net.minecraft.world.entity.player.Player player)) {
            return 0;
        }

        Optional<Holder.Reference<Enchantment>> enchantment =
                hook.level()
                        .registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .get(VELOCITYMACE$STICKY_BOBBER_KEY);

        if (enchantment.isEmpty()) {
            return 0;
        }

        int mainHandLevel = EnchantmentHelper.getItemEnchantmentLevel(
                enchantment.get(),
                player.getMainHandItem()
        );

        int offHandLevel = EnchantmentHelper.getItemEnchantmentLevel(
                enchantment.get(),
                player.getOffhandItem()
        );

        return Math.max(mainHandLevel, offHandLevel);
    }

    /**
     * Validate that the recorded block still provides a collision surface.
     * This deliberately does not try to rediscover the original hit face
     * from the recorded hit location.
     */
    @Unique
    private boolean velocityMace$isRecordedBlockValid(FishingHook hook) {
        if (velocityMace$stuckBlockPos == null
                || velocityMace$stuckFace == null
                || velocityMace$hitLocation == null
                || velocityMace$anchorPosition == null) {
            return false;
        }

        BlockPos pos = velocityMace$stuckBlockPos;

        if (!hook.level().hasChunkAt(pos)) {
            return false;
        }

        BlockState state = hook.level().getBlockState(pos);

        return !state.getCollisionShape(hook.level(), pos).isEmpty();
    }

    @Unique
    private void velocityMace$clearStickyState() {
        velocityMace$stuck = false;
        velocityMace$stuckBlockPos = null;
        velocityMace$stuckFace = null;
        velocityMace$hitLocation = null;
        velocityMace$anchorPosition = null;
    }
}
