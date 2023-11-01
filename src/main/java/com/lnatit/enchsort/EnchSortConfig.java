package com.lnatit.enchsort;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

import static com.lnatit.enchsort.EnchSort.LOGGER;
import static com.lnatit.enchsort.EnchSort.MOD_NAME;
import static net.minecraft.ChatFormatting.getByName;

public class EnchSortConfig
{
    public static ForgeConfigSpec CLIENT_CONFIG;
    public static ForgeConfigSpec.BooleanValue SORT_BY_LEVEL;
    public static ForgeConfigSpec.BooleanValue INDEPENDENT_TREASURE;
    public static ForgeConfigSpec.BooleanValue REVERSE_TREASURE;
    public static ForgeConfigSpec.BooleanValue ASCENDING_SORT;

    static
    {
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
        ASCENDING_SORT = BUILDER
                .comment(" Sort the enchantments in ascending order",
                         " default: false"
                )
                .define("ascendingSort", false);

        CLIENT_CONFIG = BUILDER.build();
    }

    private static Style parseFormatList(List<String> formats)
    {
        Style style = Style.EMPTY;
        for (String fElement : formats)
        {
            fElement = fElement.toUpperCase();

            ChatFormatting format;
            format = getByName(fElement);
            if (format != null)
            {
                style = style.applyFormat(format);
                continue;
            }

            TextColor color = TextColor.parseColor(fElement);
            if (color != null)
            {
                style = style.withColor(color);
                continue;
            }

            LOGGER.warn("Format element " + fElement + " parse failed, please check config file.");
        }

        return style;
    }
}
