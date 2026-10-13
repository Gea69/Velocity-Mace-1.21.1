package com.gea69.velocitymace;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
        modid = "velocitymace",
        value = Dist.CLIENT
)
public final class PrimedStrikeClient {

    private static final int COOLDOWN_TICKS = 100;

    private static final double PRIMED_FOV_MULTIPLIER = 0.85D;

    /*
     * Controls how quickly the FOV approaches its current target.
     * A time-based interpolation keeps the transition consistent
     * across different frame rates.
     */
    private static final double FOV_TRANSITION_SECONDS = 0.12D;

    private static boolean primed = false;
    private static double currentFovStrength = 0.0D;
    private static long lastFovUpdateNanos = 0L;

    private PrimedStrikeClient() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (!canPrime(minecraft, player)) {
            primed = false;
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        int level = PrimedStrike.getLevel(weapon);
        primed = true;

        double range = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)
                + PrimedStrike.getAdditionalRange(level);

        Entity target = PrimedStrikeTargeting.findTarget(player, range);
        if (target == null) {
            return;
        }

        player.swing(InteractionHand.MAIN_HAND);
        PrimedStrikeNetworkClient.fire(target.getId());

        player.getCooldowns().addCooldown(weapon.getItem(), COOLDOWN_TICKS);
        primed = false;
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Minecraft minecraft = Minecraft.getInstance();

        /*
         * Re-check the live equipment and input state while rendering.
         * This avoids retaining an ItemStack reference and makes the zoom
         * start returning to normal if the mace disappears, is swapped out,
         * a GUI opens, or the cooldown starts.
         */
        boolean shouldZoom = canPrime(minecraft, minecraft.player) && primed;
        double targetStrength = shouldZoom ? 1.0D : 0.0D;

        long now = System.nanoTime();
        if (lastFovUpdateNanos == 0L) {
            lastFovUpdateNanos = now;
        }

        double elapsedSeconds = Math.max(
                0.0D,
                (now - lastFovUpdateNanos) / 1_000_000_000.0D
        );
        lastFovUpdateNanos = now;

        /*
         * Exponential interpolation is smooth and frame-rate independent.
         * Clamp large pauses so returning from a pause or lag spike does
         * not cause an abrupt jump.
         */
        elapsedSeconds = Math.min(elapsedSeconds, 0.05D);
        double blend = 1.0D - Math.exp(-elapsedSeconds / FOV_TRANSITION_SECONDS);
        currentFovStrength += (targetStrength - currentFovStrength) * blend;

        if (Math.abs(targetStrength - currentFovStrength) < 0.001D) {
            currentFovStrength = targetStrength;
        }

        /*
         * Minecraft's FoV Effects option scales the strength of this effect:
         */
        double fovEffectScale = minecraft.options.fovEffectScale().get();
        double effectiveStrength = currentFovStrength
                * Math.max(0.0D, Math.min(1.0D, fovEffectScale));

        double multiplier = 1.0D
                - ((1.0D - PRIMED_FOV_MULTIPLIER) * effectiveStrength);

        event.setFOV(event.getFOV() * multiplier);
    }

    private static boolean canPrime(Minecraft minecraft, LocalPlayer player) {
        if (player == null || minecraft.level == null || minecraft.screen != null) {
            return false;
        }

        if (!minecraft.options.keyAttack.isDown()) {
            return false;
        }

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty() || PrimedStrike.getLevel(weapon) <= 0) {
            return false;
        }

        return !player.getCooldowns().isOnCooldown(weapon.getItem());
    }
}