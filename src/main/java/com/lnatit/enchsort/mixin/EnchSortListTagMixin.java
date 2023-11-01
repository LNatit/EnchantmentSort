package com.lnatit.enchsort.mixin;

import com.lnatit.enchsort.api.IReachableListTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ListTag.class)
public class EnchSortListTagMixin implements IReachableListTag
{
    @Shadow
    @Final
    private List<Tag> list;

    @Override
    public List<Tag> getListRaw()
    {
        return this.list;
    }
}
