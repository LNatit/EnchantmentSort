package com.lnatit.enchsort.mixin;

import com.lnatit.enchsort.EnchSortRule;
import com.lnatit.enchsort.api.IReachableListTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.lnatit.enchsort.EnchSort.HIDE_KEY;

@Mixin(ItemStack.class)
public class EnchSortItemStackMixin
{
    @Inject(
            id = "sortEnchantment",
            method = "getEnchantmentTags",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true
    )
    private void sortEnchantment(CallbackInfoReturnable<ListTag> cir)
    {
        ListTag enchs = cir.getReturnValue();
        if (!enchs.isEmpty() && !HIDE_KEY.isDown())
        {
            ((IReachableListTag) enchs).getListRaw().sort(EnchSortRule.EnchComparator.getInstance());
            cir.setReturnValue(enchs);
        }
    }
}
