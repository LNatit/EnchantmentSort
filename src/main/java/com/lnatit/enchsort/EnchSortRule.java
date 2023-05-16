package com.lnatit.enchsort;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.lnatit.enchsort.EnchSort.LOGGER;
import static com.lnatit.enchsort.EnchSort.MOD_ID;

public class EnchSortRule
{
    public static File RULE_FILE;
    public static Toml RULE_TOML;

    public static final String FILE_NAME = MOD_ID + "-rule.toml";
    public static final String LIST_KEY = "entries";
    public static final EnchProporty DEFAULT_PROP = new EnchProporty();
    public static final HashMap<String, EnchProporty> ENCH_RANK = new HashMap<>();

    public static void initRule()
    {
        RULE_FILE = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME).toFile();

        try
        {
            if (RULE_FILE.exists() || RULE_FILE.createNewFile())
                writeDefault();
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to create or write " + FILE_NAME + "!!!");
            LOGGER.error(e.getStackTrace());
        }

        RULE_TOML = new Toml().read(RULE_FILE);

        parseRule();
        EnchSortConfig.EnchComparator.initComparator();
    }

    public static void parseRule()
    {
        int size, index;
        List<Toml> tomlList = RULE_TOML.getTables(LIST_KEY);
        size = tomlList.size();

        for (index = 0; index < size; index++)
        {
            Toml entry = tomlList.get(index);
            EnchProporty prop = entry.to(EnchProporty.class);
            prop.sequence = size - index;
            ENCH_RANK.put(prop.name, prop);
        }

        if (ENCH_RANK.size() == index)
            LOGGER.info("Parsed " + index + " enchantments successful!");
        else LOGGER.warn("Parse count dismatch!!! There are " + (index - ENCH_RANK.size()) + " repeats.");
    }

    private static void writeDefault() throws IOException
    {
        TomlWriter writer = new TomlWriter.Builder()
                .indentValuesBy(0)
                .indentTablesBy(0)
                .build();

        List<Map<String, Object>> entry = new ArrayList<>();

        for (Enchantment ench : ForgeRegistries.ENCHANTMENTS)
        {
            Map<String, Object> elem = new LinkedHashMap<>();
            ResourceLocation rl = EnchantmentHelper.getEnchantmentId(ench);
            if (rl == null)
            {
                LOGGER.error("Failed to get enchantment: " + ench.getDescriptionId() + "!!!");
                continue;
            }
            elem.put(EnchProporty.NAME, rl.toString());
            elem.put(EnchProporty.MAX_LVL, ench.getMaxLevel());

            entry.add(elem);
        }

        Map<String, List<Map<String, Object>>> default_sequence = new HashMap<>();
        default_sequence.put(LIST_KEY, entry);

        writer.write(default_sequence, RULE_FILE);
    }

    protected static class EnchProporty
    {
        private String name;
        private int sequence;
        private int max_lvl;

        public static final String NAME = "name";
        public static final String MAX_LVL = "max_lvl";

        private EnchProporty()
        {
            name = "null";
            sequence = 0;
            max_lvl = 0;
        }

        public int getSequence()
        {
            return sequence;
        }

        public int getMaxLevel()
        {
            return max_lvl;
        }
    }
}
