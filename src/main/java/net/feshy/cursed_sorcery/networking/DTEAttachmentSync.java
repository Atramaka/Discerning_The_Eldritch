package net.feshy.cursed_sorcery.networking;

import net.feshy.cursed_sorcery.registries.DTEAttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;

import static net.feshy.cursed_sorcery.registries.DTEAttachmentRegistry.DEVOURED_ENTITIES;

public class DTEAttachmentSync {
    /***
     * Setters & Getters
     */
    // Devouring
    public static int getDevour(LivingEntity entity)
    {
        return entity.getData(DEVOURED_ENTITIES);
    }

    public static void setDevour(int val, LivingEntity entity)
    {
        entity.getData(DEVOURED_ENTITIES);
        entity.setData(DTEAttachmentRegistry.DEVOURED_ENTITIES, entity.getData(DEVOURED_ENTITIES) + val);
    }

    public static void resetDevour(LivingEntity entity)
    {
        entity.getData(DEVOURED_ENTITIES);
        entity.setData(DTEAttachmentRegistry.DEVOURED_ENTITIES, 0);
    }
}
