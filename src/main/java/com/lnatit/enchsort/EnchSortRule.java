package com.lnatit.enchsort;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lnatit.enchsort.ApotheosisSupport.getMaxLevel;
import static com.lnatit.enchsort.ApotheosisSupport.isTreasure;
import static com.lnatit.enchsort.EnchSort.LOGGER;
import static com.lnatit.enchsort.EnchSort.MOD_ID;

public class EnchSortRule
{
    private static File RULE_FILE;
    private static Toml RULE_TOML;

    private static final String FILE_NAME = MOD_ID + "-rule.toml";
    private static final String LIST_KEY = "entries";
    private static final EnchProperty DEFAULT_PROP = new EnchProperty();
    private static final HashMap<String, EnchProperty> ENCH_RANK = new HashMap<>();

    public static void initRule()
    {
        RULE_FILE = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME).toFile();

        try
        {
            try
            {
                if (!RULE_FILE.exists() || RULE_FILE.createNewFile())
                    writeDefault();

                RULE_TOML = new Toml().read(RULE_FILE);
            }
            catch (RuntimeException e)
            {
                LOGGER.warn(FILE_NAME + " contains invalid toml, try regenerating...");
                writeDefault();
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to create or write " + FILE_NAME + "!!!");
            LOGGER.error(Arrays.toString(e.getStackTrace()));
        }

        parseRule();
        EnchComparator.initComparator();
    }

    public static void parseRule()
    {
        int size, index;
        List<Toml> tomlList = RULE_TOML.getTables(LIST_KEY);
        size = tomlList == null ? 0 : tomlList.size();

        for (index = 0; index < size; index++)
        {
            Toml entry = tomlList.get(index);
            EnchProperty prop = entry.to(EnchProperty.class);
            prop.sequence = size - index;
            ENCH_RANK.put(prop.name, prop);
        }

        if (ENCH_RANK.size() == index)
            LOGGER.info("Parsed " + index + " enchantments successful!");
        else LOGGER.warn("Parse count mismatch!!! There are " + (index - ENCH_RANK.size()) + " repeats.");
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
            elem.put(EnchProperty.NAME, rl.toString());
            elem.put(EnchProperty.MAX_LVL, getMaxLevel(ench));

            entry.add(elem);
        }

        Map<String, List<Map<String, Object>>> default_sequence = new HashMap<>();
        default_sequence.put(LIST_KEY, entry);

        writer.write(default_sequence, RULE_FILE);
    }

    public static EnchProperty getPropById(String id)
    {
        return ENCH_RANK.getOrDefault(id, DEFAULT_PROP);
    }

    protected static class EnchProperty
    {
        private final String name;
        private int sequence;
        private final int max_lvl;

        public static final String NAME = "name";
        public static final String MAX_LVL = "max_lvl";

        private EnchProperty()
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

    public static class EnchComparator implements Comparator<Tag>
    {
        private static int maxEnchLvl;
        private static int enchCount;
        private static final EnchComparator INSTANCE = new EnchComparator();

        private EnchComparator()
        {
        }

        public static Comparator<Tag> getInstance()
        {
            if (EnchSortConfig.ASCENDING_SORT.get())
                return INSTANCE;
            else return INSTANCE.reversed();
        }

        public static void initComparator()
        {
            enchCount = ForgeRegistries.ENCHANTMENTS.getKeys().size();
            if (enchCount == 0)
                LOGGER.warn("Enchantments...  Where are the enchantments???!");

            maxEnchLvl = 1;
            for (Enchantment ench : ForgeRegistries.ENCHANTMENTS)
            {
                ResourceLocation rl = EnchantmentHelper.getEnchantmentId(ench);
                if (rl == null)
                {
                    LOGGER.error("Failed to get enchantment: " + ench.getDescriptionId() + "!!!");
                    continue;
                }
                int maxLevel = Math.max(getMaxLevel(ench), getPropById(rl.toString()).getMaxLevel());
                if (maxLevel > maxEnchLvl)
                    maxEnchLvl = maxLevel;
            }
            LOGGER.debug("Max enchantment level is " + maxEnchLvl + ".");
        }

        @Override
        public int compare(Tag o1, Tag o2)
        {
            if (!(o1 instanceof CompoundTag && o2 instanceof CompoundTag))
                return 0;

            int r1 = 0;
            int r2 = 0;
            AtomicInteger ret = new AtomicInteger();
            ResourceLocation e1 = EnchantmentHelper.getEnchantmentId((CompoundTag) o1);
            ResourceLocation e2 = EnchantmentHelper.getEnchantmentId((CompoundTag) o2);

            if (e1 == null)
                LOGGER.error("Failed to get enchantment: " + ((CompoundTag) o1).getString("id") + "!!!");
            else r1 = getPropById(e1.toString()).getSequence();
            if (e2 == null)
                LOGGER.error("Failed to get enchantment: " + ((CompoundTag) o2).getString("id") + "!!!");
            else r2 = getPropById(e2.toString()).getSequence();

            ret.set(r1 - r2);

            if (EnchSortConfig.SORT_BY_LEVEL.get())
                ret.addAndGet((EnchantmentHelper.getEnchantmentLevel(
                        (CompoundTag) o1) - EnchantmentHelper.getEnchantmentLevel((CompoundTag) o2)) * enchCount);

            if (EnchSortConfig.INDEPENDENT_TREASURE.get())
            {
                int treasureModify = maxEnchLvl * enchCount;
                if (EnchSortConfig.REVERSE_TREASURE.get())
                    treasureModify = -treasureModify;


                int finalTreasureModify = treasureModify;
                ForgeRegistries.ENCHANTMENTS.getDelegate(e1).ifPresent((ench) -> ret.addAndGet(
                        -(isTreasure(ench.get()) ? finalTreasureModify : 0)));
                ForgeRegistries.ENCHANTMENTS.getDelegate(e2).ifPresent((ench) -> ret.addAndGet(
                        (isTreasure(ench.get()) ? finalTreasureModify : 0)));

//                if (isTreasure(o1.getKey()))
//                    ret.addAndGet(-treasureModify);
//                if (isTreasure(o2.getKey()))
//                    ret.addAndGet(treasureModify);
            }

            return ret.get();
        }
    }
}
