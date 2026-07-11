package net.feshy.cursed_sorcery.items.weapons;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.feshy.cursed_sorcery.registries.SpellRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CataclysmBladeItem extends MagicSwordItem implements UniqueItem {
    public CataclysmBladeItem() {
        super(
                DTEWeaponTiers.CATACLYSM,
                ItemPropertiesHelper.equipment(1).fireResistant().rarity(Rarity.RARE).attributes(ExtendedSwordItem.createAttributes(DTEWeaponTiers.CATACLYSM)),
                SpellDataRegistryHolder.of(
//                        new SpellDataRegistryHolder(SpellRegistries.BLADES_OF_RANCOR, 6)
                )
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.cursed_sorcery.ancient_item.description").
                withStyle(ChatFormatting.GRAY).
                withStyle(ChatFormatting.ITALIC));
    }
}
