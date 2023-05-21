package com.lnatit.enchsort;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.*;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;

import static com.lnatit.enchsort.EnchSort.LOGGER;
import static com.lnatit.enchsort.EnchSort.MOD_NAME;

public class EnchSortConfig
{
    public static ForgeConfigSpec CLIENT_CONFIG;
    public static ForgeConfigSpec.BooleanValue SORT_BY_LEVEL;
    public static ForgeConfigSpec.BooleanValue INDEPENDENT_TREASURE;
    public static ForgeConfigSpec.BooleanValue REVERSE_TREASURE;
    public static ForgeConfigSpec.BooleanValue ALSO_SORT_BOOK;
    public static ForgeConfigSpec.BooleanValue ASCENDING_SORT;
    public static ForgeConfigSpec.BooleanValue SNEAK_DISPLAY;
    public static ForgeConfigSpec.BooleanValue SHOW_MAX_LEVEL;
    public static ForgeConfigSpec.BooleanValue COMPATIBLE_MODE;
    public static ForgeConfigSpec.ConfigValue<List<String>> MAX_LEVEL_FORMAT;
    public static ForgeConfigSpec.BooleanValue HIGHLIGHT_TREASURE;
    public static ForgeConfigSpec.ConfigValue<List<String>> TREASURE_FORMAT;

    public static final List<String> DEFAULT_FORMAT = new ArrayList<>();
    public static Style MAX_LEVEL, TREASURE;

    static
    {
        DEFAULT_FORMAT.add("DARK_GRAY");
        MAX_LEVEL = Style.EMPTY;
        TREASURE = Style.EMPTY;

        ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        BUILDER.comment(" Client Settings for " + MOD_NAME).push("client");

        // DONE
        SORT_BY_LEVEL = BUILDER
                .comment(" Whether sort the enchantments by its level",
                         " default: true"
                )
                .define("sotByLevel", true);

        // DONE
        INDEPENDENT_TREASURE = BUILDER
                .comment(" Whether to sort the treasure enchantment independently",
                         " This configuration has further info, see [client.IndieTreasure]",
                         " default: true"
                )
                .define("indieTreasure", true);

        // DONE
        BUILDER.push("IndieTreasure");
        REVERSE_TREASURE = BUILDER
                .comment(" Whether to sort the treasure on reverse side",
                         " default: false"
                )
                .define("reverseTreasure", false);
        BUILDER.pop();

        // DONE
        ALSO_SORT_BOOK = BUILDER
                .comment(" Whether sort the enchantments on enchanted book",
                         " default: false"
                )
                .define("alsoSortBook", false);

        // DONE
        ASCENDING_SORT = BUILDER
                .comment(" Sort the enchantments in ascending order",
                         " default: false"
                )
                .define("ascendingSort", false);

        // Added in v1.1.4
        SNEAK_DISPLAY = BUILDER
                .comment(" Display the original order when shift pressed",
                         " default: false"
                )
                .define("sneakDisplay", false);

        // DONE
        SHOW_MAX_LEVEL = BUILDER
                .comment(" Whether to show the max level of the enchantments",
                         " This configuration has further info, see [client.ShowMaxLevel]",
                         " default: false"
                )
                .define("showMaxLevel", false);

        //Added in 1.1.5
        COMPATIBLE_MODE = BUILDER
                .comment(" Compatible mode (may cause performance losses)",
                         " Enable this config will invalidate {showMaxLevel} & {highlightTreasure}",
                         " default: false"
                )
                .define("compatibleMode", false);

        // DONE
        BUILDER.push("ShowMaxLevel");
        MAX_LEVEL_FORMAT = BUILDER
                .comment(" The format list of the max level text",
                         " Use Formatting code or RGB format (#xxxxxx)"
                )
                .define("maxLevelFormat", DEFAULT_FORMAT);
        BUILDER.pop();

        // DONE
        HIGHLIGHT_TREASURE = BUILDER
                .comment(" Whether to highlight the treasure enchantments (except curse)",
                         " This configuration has further info, see [client.HighLightTreasure]",
                         " default: false"
                )
                .define("highlightTreasure", false);

        // DONE
        BUILDER.push("HighlightTreasure");
        TREASURE_FORMAT = BUILDER
                .comment(" The format list of treasure enchantments",
                         " Use Formatting code or RGB format (#xxxxxx)"
                )
                .define("treasureFormat", DEFAULT_FORMAT);
        BUILDER.pop();

        BUILDER.pop();

        CLIENT_CONFIG = BUILDER.build();
    }

    private static Style parseFormatList(List<String> formats)
    {
        Style style = Style.EMPTY;
        for (String fElement : formats)
        {
            fElement = fElement.toUpperCase();

            TextFormatting format = TextFormatting.getByName(fElement);
            if (format != null)
            {
                style = style.applyFormat(format);
                continue;
            }

            Color color = Color.parseColor(fElement);
            if (color != null)
            {
                style = style.withColor(color);
                continue;
            }

            LOGGER.warn("Format element " + fElement + " parse failed, please check config file.");
        }

        return style;
    }

    public static void parseConfig()
    {
        if (SHOW_MAX_LEVEL.get() || HIGHLIGHT_TREASURE.get())
        {
            MAX_LEVEL = parseFormatList(MAX_LEVEL_FORMAT.get());
            TREASURE = parseFormatList(TREASURE_FORMAT.get());
            LOGGER.info("Special format parsed successfully!");
        }
    }

    public static ITextComponent getFullEnchLine(Map.Entry<Enchantment, Integer> entry)
    {
        Enchantment enchantment = entry.getKey();
        int level = entry.getValue();
        TranslationTextComponent component = new TranslationTextComponent(enchantment.getDescriptionId());

        if (enchantment.isCurse())
            component.withStyle(TextFormatting.RED);
        else if (HIGHLIGHT_TREASURE.get() && enchantment.isTreasureOnly())
            component.withStyle(TREASURE);
        else
            component.withStyle(TextFormatting.GRAY);

        if (level != 1 || enchantment.getMaxLevel() != 1)
        {
            component.append(" ").append(new TranslationTextComponent("enchantment.level." + level));
            if (SHOW_MAX_LEVEL.get())
            {
                ITextComponent maxLvl = new StringTextComponent("/")
                        .append(new TranslationTextComponent("enchantment.level." + enchantment.getMaxLevel()))
                        .setStyle(MAX_LEVEL);
                component.append(maxLvl);
            }
        }

        return component;
    }
}
