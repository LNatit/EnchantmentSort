package com.lnatit.enchsort;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.Environment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(EnchSort.MOD_ID)
public class EnchSort
{
    public static final String MOD_ID = "enchsort";
    public static final String MOD_NAME = "Enchantment Sort";

    public static final Logger LOGGER = LogUtils.getLogger();

    // TODO WIP
    public static final KeyMapping HIDE_KEY =
            new KeyMapping("key.enchsort.hide",
                           KeyConflictContext.IN_GAME,
                           InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_LSHIFT),
                           "key.categories.enchsort"
            );
    public static final KeyMapping CONFIG_KEY =
            new KeyMapping("key.enchsort.config",
                           KeyConflictContext.IN_GAME,
                           InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_HOME),
                           "key.categories.enchsort"
            );

    public EnchSort()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EnchSortConfig.CLIENT_CONFIG);

        if (Environment.get().getDist().isClient())
        {
            FMLJavaModLoadingContext
                    .get()
                    .getModEventBus()
                    .addListener(EnchSort::onKeyRegister);

            FMLJavaModLoadingContext
                    .get()
                    .getModEventBus()
                    .addListener(EventPriority.LOWEST, EnchSort::onEnchRegister);
        }
    }

    private static void onKeyRegister(RegisterKeyMappingsEvent event)
    {
        event.register(HIDE_KEY);
        event.register(CONFIG_KEY);
    }

    private static void onEnchRegister(RegisterEvent event)
    {
        if ((IForgeRegistry<?>) event.getForgeRegistry() == ForgeRegistries.ENCHANTMENTS)
            EnchSortRule.initRule();
    }

    @SuppressWarnings("all")
    private static int getHideFlags(ItemStack stack)
    {
        return stack.hasTag() && stack.getTag().contains("HideFlags", 99) ? stack.getTag().getInt(
                "HideFlags") : stack.getItem().getDefaultTooltipHideFlags(stack);
    }
}
