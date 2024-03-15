package com.lnatit.enchsort.mixin;

import com.lnatit.enchsort.EnchantmentComparator;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author TT432
 */
@Mixin(ItemStack.class)
public class MixinItemStack {
    @Inject(method = "getEnchantmentTags", at = @At("RETURN"))
    private void enchsort$getEnchantmentTags(CallbackInfoReturnable<ListTag> cir) {
        ListTag returnValue = cir.getReturnValue();
        ((CollectionTag<CompoundTag>) (Object) returnValue).sort(EnchantmentComparator.getInstance());
    }
}
