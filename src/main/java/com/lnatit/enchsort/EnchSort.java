package com.lnatit.enchsort;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                    .addListener(EventPriority.LOW,
                                 (ModConfig.ModConfigEvent event) ->
                                 {
                                     EnchSortConfig.parseConfig();
                                     EnchSortConfig.EnchComparator.InitComparator();
                                 }
                    );
        }
    }


    private static void onItemDesc(ItemTooltipEvent event)
    {
        final ItemStack stack = event.getItemStack();

        boolean forSort = stack.isEnchanted() || (EnchSortConfig.ALSO_SORT_BOOK.get() && stack.getItem() instanceof EnchantedBookItem);
        forSort = forSort || (getHideFlags(stack) & ItemStack.TooltipDisplayFlags.ENCHANTMENTS.getMask()) == 0;
        if (!forSort || event.getEntity() == null)
            return;

        int index;
        // Since it's hard to sort Component directly, sort the enchMap instead
        final List<ITextComponent> toolTip = event.getToolTip();
        Map<Enchantment, Integer> enchMap = EnchantmentHelper.getEnchantments(stack);
        final Set<Enchantment> enchs = enchMap.keySet();

        // find index & clear Enchantment Components
        for (index = 0; index < toolTip.size(); index++)
        {
            ITextComponent line = toolTip.get(index);

            if (line instanceof TranslationTextComponent)
            {
                boolean flag = false;

                for (Enchantment ench : enchs)
                    if (((TranslationTextComponent) line).getKey().equals(ench.getDescriptionId()))
                    {
                        flag = true;
                        break;
                    }

                if (flag)
                    break;
            }
        }
        if (index + enchs.size() > toolTip.size())
        {
            LOGGER.warn("Some tooltip lines are missing!!!");
            return;
        }
        toolTip.subList(index, index + enchs.size()).clear();

        // Sort the enchMap & generate toolTip
        ArrayList<Map.Entry<Enchantment, Integer>> enchArray = new ArrayList<>(enchMap.entrySet());

        enchArray.sort(EnchSortConfig.EnchComparator.getInstance());
        for (Map.Entry<Enchantment, Integer> entry : enchArray)
            toolTip.add(index++, EnchSortConfig.getFullEnchLine(entry));
    }

    private static int getHideFlags(ItemStack stack)
    {
        return stack.hasTag() && stack.getTag().contains("HideFlags", 99) ? stack.getTag().getInt("HideFlags") : 0;
    }
}
