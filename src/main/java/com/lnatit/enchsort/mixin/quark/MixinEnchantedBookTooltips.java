package com.lnatit.enchsort.mixin.quark;

import com.lnatit.enchsort.EnchSort;
import com.lnatit.enchsort.EnchSortConfig;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Pseudo
@Mixin(targets = "vazkii.quark.content.client.tooltip.EnchantedBookTooltips")
public abstract class MixinEnchantedBookTooltips {
    @Unique private static Method enchantmentSort$getItemsForEnchantment;
    @Unique private static Method enchantmentSort$getEnchantedBookEnchantments;
    @Unique private static Method enchantmentSort$getBoolean;
    @Unique private static Constructor<?> enchantmentSort$enchantedBookComponent;

    @SuppressWarnings("unchecked")
    @Inject(method = "makeTooltip", at = @At("HEAD"), cancellable = true)
    private static void makeTooltip(final RenderTooltipEvent.GatherComponents event, final CallbackInfo callback) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (!EnchSortConfig.ALSO_SORT_BOOK.get() || !EnchSortConfig.ENABLE_QUARK_COMPATIBILITY.get()) {
            return;
        }

        if (Minecraft.getInstance().player != null) {
            ItemStack stack = event.getItemStack();

            if (stack.getItem() != Items.ENCHANTED_BOOK) {
                return;
            }

            enchantmentSort$initMethods();

            if (!enchantmentSort$areMethodsLoaded()) {
                return;
            }

            List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();

            for (EnchantmentInstance enchantmentInstance : (List<EnchantmentInstance>) enchantmentSort$getEnchantedBookEnchantments.invoke(null, stack)) {
                int tooltipIndex = 0; // Need to start at the beginning already since the order of enchantments returned by this method above is different from the sort

                while (tooltipIndex < tooltip.size()) {
                    Either<FormattedText, TooltipComponent> elementAt = tooltip.get(tooltipIndex);

                    if (elementAt.left().isPresent() && elementAt.left().get() instanceof MutableComponent mutableComponent) {
                        if (mutableComponent.getContents() instanceof TranslatableContents translatableContents) {
                            String key = translatableContents.getKey();

                            if (key.equals(enchantmentInstance.enchantment.getDescriptionId())) {
                                boolean isTableOnly = (Boolean) enchantmentSort$getBoolean.invoke(null, stack, "quark:only_show_table_enchantments", false);

                                List<ItemStack> items = (List<ItemStack>) enchantmentSort$getItemsForEnchantment.invoke(null, enchantmentInstance.enchantment, isTableOnly);

                                int itemCount = items.size();
                                int lines = (int) Math.ceil((double) itemCount / 10.0);
                                int len = 3 + Math.min(10, itemCount) * 9;

                                tooltip.add(tooltipIndex + 1, Either.right((TooltipComponent) enchantmentSort$enchantedBookComponent.newInstance(len, lines * 10, enchantmentInstance.enchantment, isTableOnly)));

                                break;
                            }
                        }
                    }

                    ++tooltipIndex;
                }
            }

            callback.cancel();
        }
    }

    @Unique
    private static void enchantmentSort$initMethods() {
        if (enchantmentSort$areMethodsLoaded()) {
            return;
        }

        try {
            Class<?> enchantedBookTooltips = Class.forName("vazkii.quark.content.client.tooltip.EnchantedBookTooltips");
            enchantmentSort$getEnchantedBookEnchantments = ObfuscationReflectionHelper.findMethod(enchantedBookTooltips, "getEnchantedBookEnchantments", ItemStack.class);
            enchantmentSort$getItemsForEnchantment = ObfuscationReflectionHelper.findMethod(enchantedBookTooltips, "getItemsForEnchantment", Enchantment.class, boolean.class);

            Class<?> itemNBTHelper = Class.forName("vazkii.arl.util.ItemNBTHelper");
            enchantmentSort$getBoolean = ObfuscationReflectionHelper.findMethod(itemNBTHelper, "getBoolean", ItemStack.class, String.class, boolean.class);

            Class<?> enchantedBookComponent = Class.forName("vazkii.quark.content.client.tooltip.EnchantedBookTooltips$EnchantedBookComponent");
            enchantmentSort$enchantedBookComponent = enchantedBookComponent.getDeclaredConstructor(int.class, int.class, Enchantment.class, boolean.class);
        } catch (Exception e) {
            EnchSort.LOGGER.error("Failed at creating the Quark classes / methods", e);
        }
    }

    @Unique
    private static boolean enchantmentSort$areMethodsLoaded() {
        return enchantmentSort$getItemsForEnchantment != null && enchantmentSort$getEnchantedBookEnchantments != null && enchantmentSort$getBoolean != null && enchantmentSort$enchantedBookComponent != null;
    }
}
