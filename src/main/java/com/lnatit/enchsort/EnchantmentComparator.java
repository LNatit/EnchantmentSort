package com.lnatit.enchsort;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Comparator;

import static com.lnatit.enchsort.EnchSort.LOGGER;
import static com.lnatit.enchsort.EnchSortRule.getPropById;

/**
 * @author TT432
 */
public class EnchantmentComparator implements Comparator<CompoundTag> {
    private static int maxEnchLvl;
    private static int enchCount;
    private static final EnchantmentComparator INSTANCE = new EnchantmentComparator();

    private EnchantmentComparator() {
    }

    public static Comparator<CompoundTag> getInstance() {
        if (EnchSortConfig.ASCENDING_SORT.get())
            return INSTANCE;
        else return INSTANCE.reversed();
    }

    public static void initComparator() {
        enchCount = BuiltInRegistries.ENCHANTMENT.keySet().size();

        if (enchCount == 0)
            LOGGER.warn("Enchantments...  Where are the enchantments???!");

        maxEnchLvl = 1;
        for (Enchantment ench : BuiltInRegistries.ENCHANTMENT) {
            ResourceLocation rl = EnchantmentHelper.getEnchantmentId(ench);
            if (rl == null) {
                LOGGER.error("Failed to get enchantment: " + ench.getDescriptionId() + "!!!");
                continue;
            }
            int maxLevel = Math.max(ench.getMaxLevel(), getPropById(rl.toString()).getMaxLevel());
            if (maxLevel > maxEnchLvl)
                maxEnchLvl = maxLevel;
        }
        LOGGER.debug("Max enchantment level is " + maxEnchLvl + ".");
    }

    @Override
    public int compare(CompoundTag o1, CompoundTag o2) {
        ResourceLocation e1 = EnchantmentHelper.getEnchantmentId(o1);
        ResourceLocation e2 = EnchantmentHelper.getEnchantmentId(o2);

        var en1 = BuiltInRegistries.ENCHANTMENT.get(e1);
        var en2 = BuiltInRegistries.ENCHANTMENT.get(e2);

        if (e1 == null || en1 == null) {
            LOGGER.error("Failed to get enchantment" + o1);
            return 0;
        }

        if (e2 == null || en2 == null) {
            LOGGER.error("Failed to get enchantment: " + o2);
            return 0;
        }

        int ret = getPropById(e1.toString()).getSequence() - getPropById(e2.toString()).getSequence();

        if (EnchSortConfig.SORT_BY_LEVEL.get()) {
            var l1 = EnchantmentHelper.getEnchantmentLevel(o1);
            var l2 = EnchantmentHelper.getEnchantmentLevel(o2);
            ret += (l1 - l2) * enchCount;
        }

        if (EnchSortConfig.INDEPENDENT_TREASURE.get()) {
            int treasureModify = maxEnchLvl * enchCount;

            if (EnchSortConfig.REVERSE_TREASURE.get()) {
                treasureModify = -treasureModify;
            }

            if (en1.isTreasureOnly()) {
                ret -= treasureModify;
            }

            if (en2.isTreasureOnly()) {
                ret += treasureModify;
            }
        }

        return ret;
    }
}