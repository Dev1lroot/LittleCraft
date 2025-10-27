package fr.dev1lroot.mcmods.littlecraft.content.item;

import fr.dev1lroot.mcmods.littlecraft.content.LittleContentRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

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

        @Override
        // This tells Minecraft:
        // "Yes, you can wear this majestic puff on your booty slot."
        public EquipmentSlot getEquipmentSlot(ItemStack stack)
        {
            return EquipmentSlot.LEGS; // it’s basically pixel pants
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
                            .durability(100)
                    ));

    // Ritual method - called by the registry system
    // so the diaper can exist in the universe.
    public static void register() {}
}
