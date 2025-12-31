package net.acetheeldritchking.discerning_the_eldritch.events;

import net.acetheeldritchking.discerning_the_eldritch.networking.SprintBoostPacket;
import net.acetheeldritchking.discerning_the_eldritch.registries.DTEPotionEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "discerning_the_eldritch", value = Dist.CLIENT)
public class ClientSprintKeyHandler {
    
    private static boolean wasSprintPressed = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player != null && player.hasEffect(DTEPotionEffectRegistry.DESTRUCTIVE_RAMPAGE_EFFECT)) {
            boolean isSprintPressed = mc.options.keySprint.isDown();
            
            // Detect sprint key press (rising edge)
            if (isSprintPressed && !wasSprintPressed) {
                // Send packet to server
                PacketDistributor.sendToServer(new SprintBoostPacket());
            }
            
            wasSprintPressed = isSprintPressed;
        } else {
            wasSprintPressed = false;
        }
    }
}
