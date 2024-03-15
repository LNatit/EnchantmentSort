package com.lnatit.enchsort;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlWriter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.NightConfigWrapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.lnatit.enchsort.EnchSort.LOGGER;
import static com.lnatit.enchsort.EnchSort.MOD_ID;
import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.ChatFormatting.RED;

public class EnchSortRule {
    private static File RULE_FILE;
    private static NightConfigWrapper RULE_TOML;

    private static final String FILE_NAME = MOD_ID + "-rule.toml";
    private static final String LIST_KEY = "entries";
    private static final EnchProperty DEFAULT_PROP = new EnchProperty();
    private static final HashMap<String, EnchProperty> ENCH_RANK = new HashMap<>();

    public static void initRule() {
        RULE_FILE = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME).toFile();

        try {
            try {
                if (!RULE_FILE.exists() || RULE_FILE.createNewFile())
                    writeDefault();

                final FileConfig fileConfig = FileConfig.builder(RULE_FILE).build();
                fileConfig.load();
                fileConfig.close();
                final NightConfigWrapper configWrapper = new NightConfigWrapper(fileConfig);
                RULE_TOML = configWrapper;
            } catch (RuntimeException e) {
                LOGGER.warn(FILE_NAME + " contains invalid toml, try regenerating...");
                writeDefault();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create or write " + FILE_NAME + "!!!");
            LOGGER.error(Arrays.toString(e.getStackTrace()));
        }

        parseRule();
        EnchantmentComparator.initComparator();
    }

    public static void parseRule() {
        int size, index;
        var tomlList = RULE_TOML.getConfigList(LIST_KEY);
        size = tomlList == null ? 0 : tomlList.size();

        for (index = 0; index < size; index++) {
            var entry = tomlList.get(index);
            if (entry instanceof NightConfigWrapper ncw) {
                int finalIndex = index;
                ncw.<String>getConfigElement("name").ifPresent(n -> {
                    ncw.<Integer>getConfigElement("max_lvl").ifPresent(ml -> {
                        EnchProperty prop = new EnchProperty(n, ml);
                        prop.sequence = size - finalIndex;
                        ENCH_RANK.put(prop.name, prop);
                    });
                });
            }
        }

        if (ENCH_RANK.size() == index)
            LOGGER.info("Parsed " + index + " enchantments successful!");
        else LOGGER.warn("Parse count mismatch!!! There are " + (index - ENCH_RANK.size()) + " repeats.");
    }

    private static void writeDefault() throws IOException {
        TomlWriter writer = new TomlWriter();

        List<CommentedConfig> entry = new ArrayList<>();

        for (Enchantment ench : BuiltInRegistries.ENCHANTMENT) {
            var elem = TomlFormat.instance().createConfig(HashMap::new);
            ResourceLocation rl = EnchantmentHelper.getEnchantmentId(ench);
            if (rl == null) {
                LOGGER.error("Failed to get enchantment: " + ench.getDescriptionId() + "!!!");
                continue;
            }
            elem.set(EnchProperty.NAME, rl.toString());
            elem.set(EnchProperty.MAX_LVL, (ench.getMaxLevel()));

            entry.add(elem);
        }

        CommentedConfig config = TomlFormat.instance().createConfig(HashMap::new);
        config.set(LIST_KEY, entry);
        writer.write(config, RULE_FILE, WritingMode.REPLACE);
    }

    public static EnchProperty getPropById(String id) {
        return ENCH_RANK.getOrDefault(id, DEFAULT_PROP);
    }

    protected static class EnchProperty {
        private final String name;
        private int sequence;
        private final int max_lvl;

        public static final String NAME = "name";
        public static final String MAX_LVL = "max_lvl";

        public EnchProperty(String name, int max_lvl) {
            this.name = name;
            this.max_lvl = max_lvl;
        }

        private EnchProperty() {
            name = "null";
            sequence = 0;
            max_lvl = 0;
        }

        public int getSequence() {
            return sequence;
        }

        public int getMaxLevel() {
            return max_lvl;
        }
    }
}
