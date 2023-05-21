package com.lnatit.enchsort;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.Environment;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.lnatit.enchsort.EnchSortConfig.COMPATIBLE_MODE;
import static com.lnatit.enchsort.EnchSortConfig.SNEAK_DISPLAY;

@Mod(EnchSort.MOD_ID)
public class EnchSort
{
    public static final String MOD_ID = "enchsort";
    public static final String MOD_NAME = "Enchantment Sort";

    public static final Logger LOGGER = LogManager.getLogger();

    public EnchSort()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EnchSortConfig.CLIENT_CONFIG);

        if (Environment.get().getDist().isClient())
        {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, EnchSort::onItemDesc);

            FMLJavaModLoadingContext
                    .get()
                    .getModEventBus()
                    .addListener(EventPriority.LOW, (ModConfig.ModConfigEvent event) -> EnchSortConfig.parseConfig());

            FMLJavaModLoadingContext
                    .get()
                    .getModEventBus()
                    .addGenericListener(Enchantment.class, EventPriority.LOWEST, EnchSort::onEnchRegister);
        }
    }

    private static void onItemDesc(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();

        boolean noEntity = event.getEntity() == null;
        boolean sneakDisp = !noEntity && SNEAK_DISPLAY.get() && Screen.hasShiftDown();
        boolean noEnchs = !(stack.isEnchanted() || EnchSortConfig.ALSO_SORT_BOOK.get() && stack.getItem() instanceof EnchantedBookItem);
        boolean tagBan = (getHideFlags(stack) & ItemStack.TooltipDisplayFlags.ENCHANTMENTS.getMask()) != 0;

        if (noEntity || sneakDisp || noEnchs || tagBan)
            return;

        List<ITextComponent> toolTip = event.getToolTip();

        if (COMPATIBLE_MODE.get())
            EnchSortRule.sortCompatible(toolTip, stack);
        else EnchSortRule.sortDefault(toolTip, stack);
    }

    private static void onEnchRegister(RegistryEvent.Register<Enchantment> event)
    {
        if (event.getRegistry() == ForgeRegistries.ENCHANTMENTS)
            EnchSortRule.initRule();
    }

    @SuppressWarnings("all")
    private static int getHideFlags(ItemStack stack)
    {
        return stack.hasTag() && stack.getTag().contains("HideFlags", 99) ? stack.getTag().getInt("HideFlags") : 0;
    }
}
