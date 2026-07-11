package net.feshy.cursed_sorcery.utils;

import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.UnaryOperator;

public class DTERarities {
    public static final EnumProxy<Rarity> APOTHIC_RARITY_PROXY = new EnumProxy<>(Rarity.class,
            -1,
            "cursed_sorcery:apothic",
            (UnaryOperator<Style>) ((style) -> style.withColor(0xba1127))
            );
}
