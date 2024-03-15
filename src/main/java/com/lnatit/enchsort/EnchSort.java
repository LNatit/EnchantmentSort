package com.lnatit.enchsort;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforgespi.Environment;
import org.slf4j.Logger;

@Mod(EnchSort.MOD_ID)
public class EnchSort {
    public static final String MOD_ID = "enchsort";
    public static final String MOD_NAME = "Enchantment Sort";

    public static final Logger LOGGER = LogUtils.getLogger();

    public EnchSort(IEventBus bus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EnchSortConfig.CLIENT_CONFIG);

        if (Environment.get().getDist().isClient()) {
            bus.addListener(EventPriority.LOW, (ModConfigEvent event) -> EnchSortConfig.parseConfig());

            bus.addListener(EventPriority.LOWEST, EnchSort::onEnchRegister);
        }
    }

    private static void onEnchRegister(RegisterEvent event) {
        if (event.getRegistry() == BuiltInRegistries.ENCHANTMENT)
            EnchSortRule.initRule();
    }

    @SuppressWarnings("all")
    private static int getHideFlags(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("HideFlags", 99) ? stack.getTag().getInt("HideFlags") : stack.getItem().getDefaultTooltipHideFlags(stack);
    }
}
