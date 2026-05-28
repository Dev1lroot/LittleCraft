/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.content.item;

import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import fr.dev1lroot.mcmods.littlecraft.content.recipe.DiaperPaddingRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Consumer;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public class Diaper
{
    public static final int BASE_CAPACITY = 1000;
    public static final int MAX_CAPACITY  = 11000;

    public static class DiaperItem extends Item
    {
        public DiaperItem(Properties properties)
        {
            super(properties);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag)
        {
            super.appendHoverText(stack, context, display, consumer, flag);
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null)
            {
                CompoundTag tag = customData.copyTag();
                String design = tag.getStringOr("DESIGN", "");
                if (!design.isEmpty())
                    consumer.accept(Component.translatable("item.littlecraft.diaper.design", design));
            }

            int capacity = Diaper.getCapacity(stack);
            int used     = Diaper.getUsed(stack);

            consumer.accept(Component.translatable("item.littlecraft.diaper.capacity")
                    .append(Component.literal(" " + String.format(Locale.ENGLISH, "%,d", capacity) + "ml"))
                    .withStyle(ChatFormatting.GRAY));
            consumer.accept(Component.translatable("item.littlecraft.diaper.used")
                    .append(Component.literal(" " + String.format(Locale.ENGLISH, "%,d", used) + "ml"))
                    .withStyle(ChatFormatting.GRAY));
        }

        @Override
        public @NotNull InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand)
        {
            if (!player.level().isClientSide())
            {
                if(target instanceof Player)
                {
                    ItemStack butt = target.getItemBySlot(EquipmentSlot.LEGS);

                    if(Diaper.getUsed(stack) == 0 && stack.getDamageValue() == 0)
                    {
                        if(butt.isEmpty() || butt.getItem() instanceof DiaperItem)
                        {
                            ItemStack diaperCopy = stack.copy();

                            player.setItemInHand(hand, butt.copy());
                            target.setItemSlot(EquipmentSlot.LEGS, diaperCopy);

                            player.sendSystemMessage(Component.translatable("littlecraft.notification.item.diaper.change.success"));
                            ((Player) target).sendSystemMessage(Component.translatable("littlecraft.notification.item.diaper.change.changed_by_other"));

                            player.level().playSound(
                                    null,
                                    player.blockPosition(),
                                    SoundEvents.WOOL_BREAK,
                                    SoundSource.PLAYERS,
                                    1.0F,
                                    1.0F
                            );
                        }
                        else
                        {
                            player.sendSystemMessage(Component.translatable("littlecraft.notification.item.diaper.change.error.armor_equipped"));
                        }
                    }
                    else
                    {
                        player.sendSystemMessage(Component.translatable("littlecraft.notification.item.diaper.change.error.diaper_used"));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
    }

    // Equipment asset key pointing to assets/littlecraft/equipment/diaper.json.
    // The JSON has no layers, so HumanoidArmorLayer renders nothing — our DiaperLayer does all rendering.
    // The assetId being present is what makes HumanoidMobRenderer.getEquipmentIfRenderable() keep the
    // item in state.legsEquipment instead of stripping it to ItemStack.EMPTY.
    @SuppressWarnings("unchecked")
    private static final ResourceKey<EquipmentAsset> ASSET_KEY = ResourceKey.create(
            (ResourceKey) EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(MODID, "diaper"));

    private static CustomData defaultData()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("DESIGN", "default");
        tag.putInt("capacity", BASE_CAPACITY);
        tag.putInt("used", 0);
        return CustomData.of(tag);
    }

    public static int getCapacity(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) return data.copyTag().getIntOr("capacity", BASE_CAPACITY);
        return BASE_CAPACITY;
    }

    public static int getUsed(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) return data.copyTag().getIntOr("used", 0);
        return 0;
    }

    public static ItemStack setUsed(ItemStack stack, int value)
    {
        ItemStack result = stack.copy();
        CustomData existing = result.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        tag.putInt("used", Math.min(value, getCapacity(stack)));
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return result;
    }

    public static final DeferredItem<DiaperItem> DIAPER =
            LittleContentRegistry.ITEMS.registerItem("diaper",
                    props -> new DiaperItem(props
                            .stacksTo(1)
                            .durability(255)
                            .component(DataComponents.CUSTOM_DATA, defaultData())
                            .component(DataComponents.EQUIPPABLE,
                                    Equippable.builder(EquipmentSlot.LEGS)
                                            .setAsset(ASSET_KEY)
                                            .build())
                    ));

    public static void register()
    {
        DiaperPaddingRecipe.registerSerializer();
    }
}
