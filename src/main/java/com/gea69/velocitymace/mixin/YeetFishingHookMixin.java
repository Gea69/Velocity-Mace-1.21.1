package com.gea69.velocitymace.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(FishingHook.class)
public abstract class YeetFishingHookMixin {

    private static final double YEET_FORCE_PER_LEVEL = 0.66D;

    private static final ResourceLocation YEET_KEY =
            ResourceLocation.fromNamespaceAndPath("velocitymace", "yeet");

    private static final ResourceLocation YOINK_KEY =
            ResourceLocation.fromNamespaceAndPath("velocitymace", "yoink");

    @Shadow
    public abstract Player getPlayerOwner();

    @Shadow
    public abstract Entity getHookedIn();

    @Shadow
    protected abstract void pullEntity(Entity entity);

    /**
     * Pulls the rod wielder toward the hooked entity, or toward the bobber
     * when the bobber has zero velocity.
     */
    @Inject(
            method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD")
    )
    private void velocityMace$applyYeet(
            ItemStack rodStack,
            CallbackInfoReturnable<Integer> cir
    ) {
        FishingHook hook = (FishingHook) (Object) this;
        Level level = hook.level();

        if (level.isClientSide()) {
            return;
        }

        Player owner = this.getPlayerOwner();
        if (owner == null) {
            return;
        }

        int yeetLevel = velocityMace$getEnchantmentLevel(
                level.registryAccess(),
                rodStack,
                YEET_KEY
        );

        if (yeetLevel <= 0) {
            return;
        }

        Entity hookedEntity = this.getHookedIn();
        Vec3 targetPosition;

        if (hookedEntity != null) {
            targetPosition = hookedEntity.position()
                    .add(0.0D, hookedEntity.getBbHeight() * 0.5D, 0.0D);
        } else if (hook.getDeltaMovement().lengthSqr() == 0.0D) {
            targetPosition = hook.position();
        } else {
            return;
        }

        Vec3 direction = targetPosition.subtract(owner.position());

        if (direction.lengthSqr() == 0.0D) {
            return;
        }

        Vec3 impulse = direction.normalize()
                .scale(YEET_FORCE_PER_LEVEL * yeetLevel);

        owner.setDeltaMovement(owner.getDeltaMovement().add(impulse));
        owner.hurtMarked = true;
    }

    /**
     * Yeet without Yoink suppresses the vanilla pull on the hooked entity.
     * If both enchantments are present, the vanilla pull is preserved so
     * that Yoink can modify it.
     */
    @Redirect(
            method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/FishingHook;pullEntity(Lnet/minecraft/world/entity/Entity;)V"
            )
    )
    private void velocityMace$conditionallyPullHookedEntity(
            FishingHook instance,
            Entity hookedEntity,
            ItemStack rodStack
    ) {
        Level level = instance.level();

        int yeetLevel = velocityMace$getEnchantmentLevel(
                level.registryAccess(),
                rodStack,
                YEET_KEY
        );

        int yoinkLevel = velocityMace$getEnchantmentLevel(
                level.registryAccess(),
                rodStack,
                YOINK_KEY
        );

        if (yeetLevel > 0 && yoinkLevel <= 0) {
            return;
        }

        this.pullEntity(hookedEntity);
    }

    /**
     * Prevents the client-side hooked-player pull event when the wielder
     * has a Yeet-only fishing rod in either hand.
     */
    @Inject(
            method = "handleEntityEvent(B)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void velocityMace$cancelYeetOnlyClientPull(
            byte id,
            CallbackInfo ci
    ) {
        if (id != 31) {
            return;
        }

        FishingHook hook = (FishingHook) (Object) this;

        if (!hook.level().isClientSide()) {
            return;
        }

        Entity hookedEntity = this.getHookedIn();

        if (!(hookedEntity instanceof Player)) {
            return;
        }

        Player localPlayer = hook.level().getPlayerByUUID(
                hookedEntity.getUUID()
        );

        if (localPlayer == null || !localPlayer.isLocalPlayer()) {
            return;
        }

        Player owner = this.getPlayerOwner();

        if (owner == null) {
            return;
        }

        if (velocityMace$isYeetOnlyRod(hook.level().registryAccess(),
                owner.getMainHandItem())
                || velocityMace$isYeetOnlyRod(hook.level().registryAccess(),
                owner.getOffhandItem())) {
            ci.cancel();
        }
    }

    private static boolean velocityMace$isYeetOnlyRod(
            RegistryAccess registryAccess,
            ItemStack stack
    ) {
        int yeetLevel = velocityMace$getEnchantmentLevel(
                registryAccess,
                stack,
                YEET_KEY
        );

        int yoinkLevel = velocityMace$getEnchantmentLevel(
                registryAccess,
                stack,
                YOINK_KEY
        );

        return yeetLevel > 0 && yoinkLevel <= 0;
    }

    private static int velocityMace$getEnchantmentLevel(
            RegistryAccess registryAccess,
            ItemStack stack,
            ResourceLocation enchantmentKey
    ) {
        Optional<Holder.Reference<Enchantment>> enchantment =
                registryAccess
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .get(net.minecraft.resources.ResourceKey.create(
                                Registries.ENCHANTMENT,
                                enchantmentKey
                        ));

        return enchantment
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(
                        holder,
                        stack
                ))
                .orElse(0);
    }
}