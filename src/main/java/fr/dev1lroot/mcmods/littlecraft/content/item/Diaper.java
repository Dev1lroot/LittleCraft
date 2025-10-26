package fr.dev1lroot.mcmods.littlecraft.content.item;

import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public class Diaper
{
    // Кастомный класс предмета
    public static class DiaperItem extends Item
    {
        public DiaperItem(Properties properties)
        {
            super(properties);
        }

        @Override
        public EquipmentSlot getEquipmentSlot(ItemStack stack)
        {
            return EquipmentSlot.LEGS; // надевается как поножи
        }
    }

    // Регистрация
    public static final DeferredHolder<Item, Item> DIAPER =
            LittleContentRegistry.ITEMS.register("diaper",
                    () -> new DiaperItem(new Item.Properties().stacksTo(1)));

    // Вызов из реестра
    public static void register() {}
}
