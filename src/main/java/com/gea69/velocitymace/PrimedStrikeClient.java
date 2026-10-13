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
     * True only while:
     *
     * - the player has a Primed Strike mace equipped
     * - the attack key is held
     * - the mace is not on cooldown
     */
    private static boolean primed = false;

    private PrimedStrikeClient() {
    }

    @SubscribeEvent
    public static void onClientTick(
            ClientTickEvent.Post event
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        LocalPlayer player =
                minecraft.player;

        /*
         * Reset the primed state whenever there is no
         * usable client player/world.
         */
        if (player == null || minecraft.level == null) {
            primed = false;
            return;
        }

        /*
         * Do not maintain the primed state while a GUI is open.
         */
        if (minecraft.screen != null) {
            primed = false;
            return;
        }

        ItemStack weapon =
                player.getMainHandItem();

        int level =
                PrimedStrike.getLevel(weapon);

        /*
         * Primed Strike is not active unless the current
         * main-hand item actually has the enchantment.
         */
        if (level <= 0) {
            primed = false;
            return;
        }

        /*
         * The attack key controls the primed state.
         */
        if (!minecraft.options.keyAttack.isDown()) {
            primed = false;
            return;
        }

        /*
         * Cooldown prevents the mace from being primed again.
         */
        if (player.getCooldowns().isOnCooldown(
                weapon.getItem()
        )) {
            primed = false;
            return;
        }

        /*
         * At this point the mace is genuinely primed:
         *
         *   Primed Strike mace
         *   + LMB held
         *   + cooldown inactive
         */
        primed = true;

        double range =
                player.getAttributeValue(
                        Attributes.ENTITY_INTERACTION_RANGE
                )
                        + PrimedStrike.getAdditionalRange(level);

        Entity target =
                PrimedStrikeTargeting.findTarget(
                        player,
                        range
                );

        if (target == null) {
            return;
        }

        /*
         * Play the normal mace attack animation immediately on the client.
         */
        player.swing(
                InteractionHand.MAIN_HAND
        );

        PrimedStrikeNetworkClient.fire(
                target.getId()
        );

        /*
         * Start the local five-second cooldown immediately.
         */
        player.getCooldowns().addCooldown(
                weapon.getItem(),
                COOLDOWN_TICKS
        );

        /*
         * The attack has fired, so it is no longer primed.
         */
        primed = false;
    }

    @SubscribeEvent
    public static void onComputeFov(
            ViewportEvent.ComputeFov event
    ) {
        /*
         * Only narrow the FOV while the mace is actually
         * in the primed state.
         */
        if (!primed) {
            return;
        }

        event.setFOV(
                event.getFOV()
                        * PRIMED_FOV_MULTIPLIER
        );
    }
}