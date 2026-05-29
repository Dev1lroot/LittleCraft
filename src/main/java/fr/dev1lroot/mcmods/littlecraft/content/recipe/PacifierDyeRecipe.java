/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.recipe;

import com.mojang.serialization.MapCodec;
import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import fr.dev1lroot.mcmods.littlecraft.content.item.Pacifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

public class PacifierDyeRecipe extends CustomRecipe
{
    public static final MapCodec<PacifierDyeRecipe> CODEC = MapCodec.unit(PacifierDyeRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, PacifierDyeRecipe> STREAM_CODEC =
        StreamCodec.of(
            (buf, recipe) -> {},
            buf -> new PacifierDyeRecipe()
        );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PacifierDyeRecipe>> SERIALIZER =
        LittleContentRegistry.RECIPE_SERIALIZERS.register("pacifier_dye",
            () -> new RecipeSerializer<>(CODEC, STREAM_CODEC));

    public PacifierDyeRecipe() {}

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        int pacifierCount = 0;
        int dyeCount = 0;
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof Pacifier.PacifierItem)
                pacifierCount++;
            else if (stack.getItem() instanceof DyeItem)
                dyeCount++;
            else
                return false;
        }
        return pacifierCount == 1 && (dyeCount == 1 || dyeCount == 2);
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        ItemStack pacifierStack = ItemStack.EMPTY;
        List<DyeColor> dyeColors = new ArrayList<>();

        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof Pacifier.PacifierItem)
                pacifierStack = stack;
            else if (stack.getItem() instanceof DyeItem)
            {
                DyeColor color = stack.get(DataComponents.DYE);
                if (color != null) dyeColors.add(color);
            }
        }

        if (pacifierStack.isEmpty()) return ItemStack.EMPTY;

        int existingBody = Pacifier.getBodyColor(pacifierStack);
        int existingRing = Pacifier.getRingColor(pacifierStack);

        int newBody, newRing;
        if (dyeColors.size() == 1)
        {
            int rgb  = dyeColors.get(0).getTextureDiffuseColor() & 0x00FFFFFF;
            newBody  = Pacifier.mixColor(existingBody, rgb);
            newRing  = Pacifier.mixColor(existingRing, rgb);
        }
        else if (dyeColors.size() == 2)
        {
            int bodyRgb = dyeColors.get(0).getTextureDiffuseColor() & 0x00FFFFFF;
            int ringRgb = dyeColors.get(1).getTextureDiffuseColor() & 0x00FFFFFF;
            newBody     = Pacifier.mixColor(existingBody, bodyRgb);
            newRing     = Pacifier.mixColor(existingRing, ringRgb);
        }
        else return ItemStack.EMPTY;

        return Pacifier.setColors(pacifierStack, newBody, newRing);
    }

    @Override
    public RecipeSerializer<PacifierDyeRecipe> getSerializer() { return SERIALIZER.get(); }

    public static void registerSerializer() {}
}
