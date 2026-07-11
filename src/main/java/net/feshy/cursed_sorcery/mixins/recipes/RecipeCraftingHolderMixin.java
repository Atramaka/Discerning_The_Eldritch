package net.feshy.cursed_sorcery.mixins.recipes;

import net.feshy.cursed_sorcery.registries.DTEAttachmentRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeCraftingHolder.class)
public interface RecipeCraftingHolderMixin {
    @Inject(
            method = "setRecipeUsed(Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/RecipeHolder;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void checkForLockedRecipes(Level level, ServerPlayer players, RecipeHolder<?> recipe, CallbackInfoReturnable<Boolean> cir)
    {
        //List<ResourceLocation> recipes = new ArrayList<>();

        //recipes.add(ResourceLocation.fromNamespaceAndPath(CursedSorcery.MOD_ID, "data/cursed_sorcery/recipe/insanity/apple.json"));

        if (!players.getData(DTEAttachmentRegistry.IS_INSANE) && recipe.id().getPath().equals("insanity/"))
        {
            cir.setReturnValue(false);
        }
    }
}
