package fr.dev1lroot.mcmods.littlecraft.event;

import fr.dev1lroot.mcmods.littlecraft.content.item.Diaper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@EventBusSubscriber(modid = "littlecraft")
public class EquipmentEventHandler
{
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event)
    {
        LivingEntity entity = event.getEntity();
        EquipmentSlot slot = event.getSlot();
        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        // Yay! Something got equipped! Let's check if it's a diaper, hehe~
        if (to.getItem() instanceof Diaper.DiaperItem)
        {
            CustomData customData = to.get(DataComponents.CUSTOM_DATA);

            // Oh no, no custom data? Let's fix that before the diaper gets cranky!
            if (customData == null)
            {
                customData = CustomData.EMPTY;
            }

            // Time to grab (or make) a shiny new NBT tag for our cute diaper~
            CompoundTag tag = customData.copyTag();

            // TODO: Wiggle wiggle! Make diaper equip event happen here and maybe sprinkle some NBT magic!
        }

        // Awww... something got unequipped. Let's see if the diaper went bye-bye~
        if (from.getItem() instanceof Diaper.DiaperItem && to.isEmpty())
        {
            // TODO: Oh no, diaper off! Fire unequip event and clean up that NBT mess~
        }
    }
}