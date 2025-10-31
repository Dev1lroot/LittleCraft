package fr.dev1lroot.mcmods.littlecraft.content.item;

import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// The Diaper class
// -----------------
// This is the holy grail of pixel pants.
// Right now it’s just one simple diaper,
// but one day it might evolve into a whole wardrobe (abstract class)
// of fancy nappies: rainbow ones, sparkly ones, who knows?

public class Diaper
{
    // The actual Diaper item class.
    // We don’t use ArmorItem because it insists on doing
    // too many fashion shows we don’t need.
    public static class DiaperItem extends Item
    {
        // Constructor: same as a normal Minecraft item,
        // but with way more dignity because it’s a diaper.
        public DiaperItem(Properties properties)
        {
            super(properties);
        }

        // This tells Minecraft:
        // "Yes, you can wear this majestic puff on your booty slot."
        @Override
        public EquipmentSlot getEquipmentSlot(ItemStack stack)
        {
            return EquipmentSlot.LEGS; // it’s basically pixel pants
        }

        // Adds all important stats to your diapee
        @Override
        public void verifyComponentsAfterLoad(ItemStack stack)
        {
            super.verifyComponentsAfterLoad(stack);

            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

            // Если нет — создаём новый
            if (customData == null)
            {
                customData = CustomData.EMPTY;
            }

            // Достаём или создаём NBT-тег
            CompoundTag tag = customData.copyTag();

            if (!tag.contains("times_peed"))
            {
                // Будем знать все о том как использовался данный предмет
                tag.putInt("times_peed", 0);
                tag.putInt("times_pooped", 0);
                tag.putInt("absorbency", 100);

                // Обновляем компонент
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }

        // Diaper Change Logic
        @Override
        public @NotNull InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand)
        {
            // This is for serverside
            if (!player.level().isClientSide)
            {
                // And for players only
                if(target instanceof Player)
                {
                    ItemStack butt = target.getItemBySlot(EquipmentSlot.LEGS);

                    // If the diaper we're trying to use is clean (YOU CAN'T PUT ON DIAPER YOU WORE)
                    if(stack.getDamageValue() == 0)
                    {
                        // If only the target player is wearing diapers or doesn't wear anything at all
                        if(butt.isEmpty() || butt.getItem() instanceof DiaperItem)
                        {
                            // Создаем копию подгузника
                            ItemStack diaperCopy = stack.copy();

                            // Replacing items
                            player.setItemInHand(hand, butt.copy()); // игрок получает то, что было надето на цели
                            target.setItemSlot(EquipmentSlot.LEGS, diaperCopy); // цель получает копию твоего предмета

                            // Notifying players about their actions
                            player.displayClientMessage(Component.translatable("littlecraft.notification.item.diaper.change.success"), true);
                            ((Player) target).displayClientMessage(Component.translatable("littlecraft.notification.item.diaper.change.changed_by_other"), true);

                            // Playing sound
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
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
    }

    // Registry magic spell:
    // Makes the game officially recognize our diaper as a real item.
    // StacksTo(1) = you can’t hoard diapers like cobblestone,
    // Durability(100) = soggy meter goes from 0 to 100 before "uh oh".
    public static final DeferredHolder<Item, Item> DIAPER =
            LittleContentRegistry.ITEMS.register("diaper",
                    () -> new DiaperItem(new Item.Properties()
                            .stacksTo(1)
                            .durability(255)
                    ));

    // Ritual method - called by the registry system
    // so the diaper can exist in the universe.
    public static void register() {}
}
